package com.example.Util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.tools.generic.DateTool;
import org.jxls.area.Area;
import org.jxls.builder.xls.XlsCommentAreaBuilder;
import org.jxls.common.CellRef;
import org.jxls.formula.StandardFormulaProcessor;
import org.jxls.transform.poi.PoiTransformer;
import org.jxls.util.TransformerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.jxls.common.Context;



import com.example.models.BaseReportItem;
import com.example.models.StopReportItem;
import com.example.models.ModelosBases.Device;
import com.example.models.ModelosBases.Position;
import com.example.repositories.PositionRepository;


@Component
public class ReportUtils {

    @Value("${report.period.limit.seconds:86400}")
    private long reportPeriodLimitSeconds;

    private final PositionRepository positionRepository;
    private final VelocityEngine velocityEngine;

    public ReportUtils(PositionRepository positionRepository) {
        this.positionRepository = positionRepository;
        this.velocityEngine = new VelocityEngine();
        this.velocityEngine.init(); 
    }

    public void checkPeriodLimit(Date from, Date to) {
        long limit = reportPeriodLimitSeconds * 1000;
        if (limit > 0 && (to.getTime() - from.getTime()) > limit) {
            throw new IllegalArgumentException("Time period exceeds the limit");
        }
    }

    public <T extends BaseReportItem> List<T> detectTripsAndStops(Device device, Date from, Date to, Class<T> clazz) {

        List<Position> positions = positionRepository.findByDeviceIdAndFixTimeBetween(device.getId(), from, to);
        List<T> result = new ArrayList<>();

        if (clazz.equals(StopReportItem.class)) {
            for (int i = 1; i < positions.size(); i++) {
                Position prev = positions.get(i - 1);
                Position curr = positions.get(i);

                long duration = curr.getFixTime().getTime() - prev.getFixTime().getTime();

                // Por simplicidad, consideramos como "stop" si la velocidad fue 0 en ambas posiciones
                if (prev.getSpeed() == 0 && curr.getSpeed() == 0 && duration >= 60000) { // mínimo 1 minuto

                    StopReportItem stop = new StopReportItem();
                    stop.setDeviceId(device.getId());
                    stop.setDeviceName(device.getName());

                    stop.setPositionId(prev.getId());
                    stop.setLatitude(prev.getLatitude());
                    stop.setLongitude(prev.getLongitude());
                    stop.setAddress(prev.getAddress());
                    stop.setStartTime(prev.getFixTime());
                    stop.setEndTime(curr.getFixTime());
                    stop.setDuration(duration);
                    stop.setStartOdometer(0); // por ahora, fijo
                    stop.setEndOdometer(0);   // por ahora, fijo

                    result.add(clazz.cast(stop));
                }
            }
        }

        return result;
    }
    public Context initializeContext(long userId) {
        Context context = PoiTransformer.createInitialContext();
        context.putVar("distanceUnit", "km");
        context.putVar("speedUnit", "km/h");
        context.putVar("volumeUnit", "L");
        context.putVar("webUrl", "http://localhost:8081");
    
        context.putVar("dateTool", new DateTool()); // ✅ esta línea es clave
        context.putVar("locale", Locale.getDefault());
        context.putVar("timezone", TimeZone.getDefault());
        context.putVar("numberTool", new Object()); // puede dejarse así si no se usa
        context.putVar("bracketsRegex", "[\\{\\}\"]");
        return context;
    }
    

    public void processTemplateWithSheets(InputStream templateStream, OutputStream outputStream, Context context) throws IOException {
        var transformer = TransformerFactory.createTransformer(templateStream, outputStream);
        List<Area> xlsAreas = new XlsCommentAreaBuilder(transformer).build();
        for (Area area : xlsAreas) {
            area.applyAt(new CellRef(area.getStartCellRef().getCellName()), context);
            area.setFormulaProcessor(new StandardFormulaProcessor());
            area.processFormulas();
        }
        transformer.deleteSheet(xlsAreas.get(0).getStartCellRef().getSheetName());
        transformer.write();
    }
}



package com.example.helper;

import com.example.models.ModelosBases.Device;

import java.util.Map;

public class AttributeUtil {

    public interface Provider {
        Map<String, Object> getAttributes();
    }

    public static <T> T lookup(Provider provider, String key, T defaultValue) {
        if (provider.getAttributes().containsKey(key)) {
            try {
                return (T) provider.getAttributes().get(key);
            } catch (ClassCastException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public static class DeviceProvider implements Provider {
        private final Device device;

        public DeviceProvider(Device device) {
            this.device = device;
        }

        @Override
        public Map<String, Object> getAttributes() {
            return device.getAttributes();
        }
    }
}

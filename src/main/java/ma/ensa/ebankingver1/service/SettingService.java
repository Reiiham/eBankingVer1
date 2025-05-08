package ma.ensa.ebankingver1.service;

import ma.ensa.ebankingver1.model.GlobalSetting;
import ma.ensa.ebankingver1.repository.SettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SettingService {

    @Autowired
    private SettingRepository globalSettingRepository;

    public String getSettingValue(String key) {
        return globalSettingRepository.findByKey(key)
                .orElseThrow(() -> new IllegalArgumentException("Setting not found"))
                .getValue();
    }

    public GlobalSetting updateSetting(String key, String value) {
        Optional<GlobalSetting> existingSetting = globalSettingRepository.findByKey(key);
        GlobalSetting setting = existingSetting.orElse(new GlobalSetting());
        setting.setKey(key);
        setting.setValue(value);
        return globalSettingRepository.save(setting);
    }
    public List<GlobalSetting> getAllSettings() {
        return globalSettingRepository.findAll();
    }

    public GlobalSetting getSetting(String key) {
        return globalSettingRepository.findByKey(key)
                .orElseThrow(() -> new IllegalArgumentException("Setting not found"));
    }
}

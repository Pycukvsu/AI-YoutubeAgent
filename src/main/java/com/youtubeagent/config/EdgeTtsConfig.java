package com.youtubeagent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "edge-tts")
public class EdgeTtsConfig {

    private String pythonPath = "python3";
    private String wrapperScript = "scripts/edge_tts_wrapper.py";
    private String voice = "ru-RU-DmitryNeural";
    private String rate = "+0%";

    public String getPythonPath() { return pythonPath; }
    public void setPythonPath(String pythonPath) { this.pythonPath = pythonPath; }

    public String getWrapperScript() { return wrapperScript; }
    public void setWrapperScript(String wrapperScript) { this.wrapperScript = wrapperScript; }

    public String getVoice() { return voice; }
    public void setVoice(String voice) { this.voice = voice; }

    public String getRate() { return rate; }
    public void setRate(String rate) { this.rate = rate; }
}

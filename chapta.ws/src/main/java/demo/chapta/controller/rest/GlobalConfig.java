package demo.chapta.controller.rest;

import java.util.List;

public class GlobalConfig {

	private List<IOrcConfig> orcConfigs;
	private String url;
	private String tag;
	
	public void setTag(String val){ this.tag = val; }
	public String getTag(){ return this.tag; }
	
	public List<IOrcConfig> getOrcConfigs() { return orcConfigs; }
	public void setOrcConfigs(List<IOrcConfig> orcConfigs) { this.orcConfigs = orcConfigs; }
	
	public String getRepository() { return url; }
	public void setRepository(String url) { this.url = url; }
}

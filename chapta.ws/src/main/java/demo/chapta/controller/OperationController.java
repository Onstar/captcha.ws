package demo.chapta.controller;

import java.text.ParseException;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import demo.chapta.model.Bid;
import demo.chapta.model.BidOperation;
import demo.chapta.model.Client;
import demo.chapta.model.Operation;
import demo.chapta.model.ScreenConfig;
import demo.chapta.service.IClientService;
import demo.chapta.service.IOperationService;
import demo.chapta.service.IScreenConfigService;

@RequestMapping(value = "/command/operation")
@Controller
public class OperationController {

	private static final Logger logger = LoggerFactory.getLogger(OperationController.class);
	
	private ObjectMapper objMapper = new ObjectMapper();
	private IOperationService operationService;
	public void setOperationService(IOperationService operationService) {
		this.operationService = operationService;
	}
	
	private IScreenConfigService screenService;
	public void setScreenConfigService(IScreenConfigService service){
		this.screenService = service;
	}
	
	private IClientService clientService;
	public void setClientService(IClientService clientServ){
		this.clientService = clientServ;
	}
	
	/***
	 * 创建ScreenConfig（来自HOST）
	 * @param fromHost
	 * @param bid
	 */
	@RequestMapping(value = "/screenconfig/BID/accept.do",method=RequestMethod.POST)
	@ResponseBody
	public void acceptScreenConfig(@RequestParam("fromHost")String fromHost, @RequestBody Bid bid){

		logger.info("accept screenConfig BID from 【{}】", fromHost);
		ScreenConfig config = new ScreenConfig();
		try {
			config.setFromHost(fromHost);
			config.setCategory("BID");
			config.setJsonContent(objMapper.writeValueAsString(bid));
			this.screenService.save(config);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/***
	 * 删除Operation
	 * @param opsID
	 */
	@RequestMapping(value = "/{operationID}",method=RequestMethod.DELETE)
	@ResponseBody
	public void deleteOperation(@PathVariable("operationID") int opsID){
		
		this.operationService.deleteByID(opsID);
	}
	
	/***
	 * 根据类型获取ScreenConfig
	 * @param category
	 * @return
	 */
	@RequestMapping(value = "/screenconfig/list.do",method=RequestMethod.GET)
	@ResponseBody
	public List<ScreenConfig> listScreenConfig(@RequestParam("category")String category){
		
		logger.info("filter screenConfig by 【{}】", category);
		return this.screenService.listByCategory(category);
	}
	
	/***
	 * 获取所有ScreenConfig
	 * @return
	 */
	@RequestMapping(value = "/screenconfig/all.do",method=RequestMethod.GET)
	@ResponseBody
	public List<ScreenConfig> listScreenConfig(){
		
		logger.info("list screenConfig");
		return this.screenService.listAll();
	}
	
	/***
	 * 新建Operation
	 * @param bid
	 */
	@RequestMapping(value = "/BID/accept.do",method=RequestMethod.POST)
	@ResponseBody
	public void acceptOperation(@RequestBody BidOperation bid){
		
		this.operationService.saveOrUpdate(bid);
	}
	
	@RequestMapping(value = "/assign.do",method=RequestMethod.POST)
	@ResponseBody
	public void assignOperation(@RequestBody AssignOperation req){

		List<Client> hosts = this.clientService.listByIPs(req.getHosts());
		this.operationService.assign2Clients(req.getOperationID(), hosts);
	}
	/***
	 * 创建Operation,同时分配到Client
	 * @param req
	 * @throws ParseException
	 */
	@RequestMapping(value = "/BID/assign.do",method=RequestMethod.POST)
	@ResponseBody
	public void acceptOperation(@RequestBody CreateBidRequest req) throws ParseException{

		ScreenConfig screenConfig = this.screenService.getByID(req.getScreenID());
		List<Client> clients = this.clientService.listByIPs(req.hosts);
		BidOperation bid = req.getBid();
		bid.setContent(screenConfig.getJsonContent());
		this.operationService.updateClientOperation(bid, clients);
	}
	
	@RequestMapping(value = "/BID/{operationID}",method=RequestMethod.PUT)
	@ResponseBody
	public String modifyOperation(@RequestBody BidOperation operation, @PathVariable("operationID") int opsID){

		this.operationService.saveOrUpdate(operation);
		return "SUCCESS";
	}
	
	@RequestMapping(value = "/{operationID}",method=RequestMethod.GET)
	public String modifyDialog(Model model, @PathVariable("operationID") int opsID){
		
		Operation ops = this.operationService.queryByID(opsID);
		model.addAttribute("operation", ops);
		return "operation/modifyDialog";
	}
	
	@RequestMapping(value = "/list",method=RequestMethod.GET)
	public String listOperation(Model model){
		
		model.addAttribute("operations", this.operationService.listAll());
		List<ScreenConfig> screens = this.screenService.listAll();
		model.addAttribute("screens", screens);
		return "operation/list";
	}
	
	@RequestMapping(value = "/screenconfig/appendOps.do",method=RequestMethod.GET)
	public String initConfig(){
		return "appendOps";
	}
	
	@RequestMapping(value = "/screenconfig/showAll.do",method=RequestMethod.GET)
	public String listScreenConfig(Model model){
		
		List<ScreenConfig> screens = this.screenService.listAll();
		model.addAttribute("screens", screens);
		return "screen/list";
	}
	
	/***
	 * 分配Operation请求
	 * @author martin
	 */
	static public class AssignOperation{
		private String[] hosts;
		int operationID;
		
		public void setHosts(String[] hosts){ this.hosts = hosts; }
		public String[] getHosts(){ return this.hosts; }
		
		public void setOperationID(int value){ this.operationID = value; }
		public int getOperationID(){ return this.operationID; }
	}
	
	static public class CreateBidRequest{
		private int screenID;
		private String[] hosts;
		private BidOperation bid;
		
		public int getScreenID() { return screenID; }
		public void setScreenID(int screenID) { this.screenID = screenID; }
		
		public String[] getHosts() { return hosts; }
		public void setHosts(String[] hosts) { this.hosts = hosts; }
		
		public BidOperation getBid() { return bid; }
		public void setBid(BidOperation bid) { this.bid = bid; }
	}
}
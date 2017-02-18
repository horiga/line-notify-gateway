package org.horiga.linenotifygateway.controller.admin;

import org.horiga.linenotifygateway.service.ManagementService;
import org.horiga.linenotifygateway.service.ManagementService.ServiceDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/console")
@Controller
public class ViewController {

    private final ManagementService managementService;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    public ViewController(ManagementService managementService) {
        this.managementService = managementService;
    }

    @GetMapping({ "/", "/index" })
    public String index() {
        return "index";
    }

    @GetMapping("/service")
    public String service(Model m) {
        m.addAttribute("services", managementService.getServices());
        return "service";
    }

    @GetMapping("/service/{sid}")
    public String serviceDetails(
            @PathVariable("sid") String sid,
            Model m) {
        ServiceDetail serviceDetails = managementService.getServiceDetails(sid);
        m.addAttribute("se", serviceDetails.getService());
        m.addAttribute("tokens", serviceDetails.getTokens());
        m.addAttribute("templates", serviceDetails.getTemplates());
        return "service-detail";
    }

    @GetMapping("/template")
    public String template(Model m) {
        return "template";
    }

    @GetMapping("/template/{template_group_identifier}")
    public String templateDetails(
            @PathVariable("template_group_identifier") String templateGroupIdentifier, Model m) {
        return "template";
    }
}

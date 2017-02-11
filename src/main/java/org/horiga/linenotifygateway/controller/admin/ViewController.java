package org.horiga.linenotifygateway.controller.admin;

import org.horiga.linenotifygateway.repository.TemplateRepository;
import org.horiga.linenotifygateway.repository.ServiceRepository;
import org.horiga.linenotifygateway.repository.TokenRepository;
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

    private final ServiceRepository serviceRepository;

    private final TokenRepository tokenRepository;

    private final TemplateRepository messageTemplateRepository;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    public ViewController(ManagementService managementService, ServiceRepository serviceRepository,
                          TokenRepository tokenRepository, TemplateRepository messageTemplateRepository) {
        this.managementService = managementService;
        this.serviceRepository = serviceRepository;
        this.tokenRepository = tokenRepository;
        this.messageTemplateRepository = messageTemplateRepository;
    }

    @GetMapping({ "/", "/index" })
    public String index() {
        return "index";
    }

    @GetMapping("/service")
    public String service(Model model) {
        model.addAttribute("services", serviceRepository.findAll());
        return "service";
    }

    @GetMapping("/service/{sid}")
    public String serviceDetail(
            @PathVariable("sid") String sid,
            Model model) {
        ServiceDetail serviceDetails = managementService.getServiceDetails(sid);
        model.addAttribute("se", serviceDetails.getService());
        model.addAttribute("tokens", serviceDetails.getTokens());
        model.addAttribute("templates", serviceDetails.getTemplates());
        return "service-detail";
    }
}

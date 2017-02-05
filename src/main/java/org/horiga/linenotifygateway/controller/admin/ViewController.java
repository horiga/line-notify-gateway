package org.horiga.linenotifygateway.controller.admin;

import org.horiga.linenotifygateway.entity.ServiceEntity;
import org.horiga.linenotifygateway.repository.MessageTemplateRepository;
import org.horiga.linenotifygateway.repository.ServiceRepository;
import org.horiga.linenotifygateway.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/admin")
@Controller
public class ViewController {

    private final ServiceRepository serviceRepository;

    private final TokenRepository tokenRepository;

    private final MessageTemplateRepository messageTemplateRepository;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    public ViewController(ServiceRepository serviceRepository,
                          TokenRepository tokenRepository, MessageTemplateRepository messageTemplateRepository) {
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
            @PathVariable("sid") String serviceId,
            Model model) {
        final ServiceEntity se = serviceRepository.findById(serviceId);
        model.addAttribute("se", se);
        model.addAttribute("to", tokenRepository.findByServiceId(serviceId));
        model.addAttribute("me", messageTemplateRepository.findTemplateByGroup(
                se.getMessageTemplateGroupId()));
        return "service-detail";
    }
}

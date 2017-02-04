package org.horiga.linenotifygateway.controller.admin;

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

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    public ViewController(ServiceRepository serviceRepository,
                          TokenRepository tokenRepository) {
        this.serviceRepository = serviceRepository;
        this.tokenRepository = tokenRepository;
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
        model.addAttribute("tokens", tokenRepository.findByServiceId(serviceId));
        return "service-detail";
    }
}

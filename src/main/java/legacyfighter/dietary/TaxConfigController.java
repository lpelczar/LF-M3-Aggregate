package legacyfighter.dietary;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class TaxConfigController {

    @Autowired
    private TaxRuleService taxRuleService;

    @GetMapping("/config")
    public Map<String, List<TaxRule>> taxConfigs() {
        return taxRuleService.findAllRules();
    }


}

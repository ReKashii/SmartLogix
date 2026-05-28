package cl.smartlogix.ms_envios.factory;

import java.time.LocalDate;

public class ExpressShipping implements ShippingMethod {
    @Override
    public Double calculateCost(Double basePrice) {
        return basePrice * 0.15; // 15% del precio base
    }

    @Override
    public LocalDate calculateEstimatedDeliveryDate() {
        return LocalDate.now().plusDays(2);
    }

    @Override
    public String getDescription() {
        return "Express Shipping";
    }
}

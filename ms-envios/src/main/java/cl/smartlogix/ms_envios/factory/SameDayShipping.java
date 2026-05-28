package cl.smartlogix.ms_envios.factory;

import java.time.LocalDate;

public class SameDayShipping implements ShippingMethod {
    @Override
    public Double calculateCost(Double basePrice) {
        return basePrice * 0.30; // 30% del precio base
    }

    @Override
    public LocalDate calculateEstimatedDeliveryDate() {
        return LocalDate.now();
    }

    @Override
    public String getDescription() {
        return "Next Day Delivery";
    }
}

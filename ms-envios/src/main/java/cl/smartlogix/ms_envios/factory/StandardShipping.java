package cl.smartlogix.ms_envios.factory;

import java.time.LocalDate;

public class StandardShipping implements ShippingMethod {
    @Override
    public Double calculateCost(Double basePrice) {
        return basePrice * 0.05; // 5% del precio base
    }

    @Override
    public LocalDate calculateEstimatedDeliveryDate() {
        return LocalDate.now().plusDays(5);
    }

    @Override
    public String getDescription() {
        return "Standard Delivery";
    }
}

package cl.smartlogix.ms_envios.factory;

import java.time.LocalDate;

public interface ShippingMethod {
    Double calculateCost(Double basePrice);
    LocalDate calculateEstimatedDeliveryDate();
    String getDescription();
}

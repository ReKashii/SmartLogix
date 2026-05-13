package cl.smartlogix.ms_pedidos.factory;

import org.springframework.stereotype.Component;

/**
 * Factory Method Pattern implementation.
 * This class encapsulates the logic for creating different types of shipping methods.
 * It allows the system to be extensible by adding new shipping types without modifying
 * the core Order service logic.
 */
@Component
public class ShippingFactory {

    public enum ShippingType {
        STANDARD, EXPRESS, NEXT_DAY
    }

    /**
     * Returns the label for the shipping type based on the provided ShippingType enum.
     * 
     * @param type The desired shipping type.
     * @return String representing the delivery method.
     */
    public String createShippingMethod(ShippingType type) {
        return switch (type) {
            case STANDARD -> "Standard Ground Shipping (3-5 business days)";
            case EXPRESS -> "Express Air Shipping (1-2 business days)";
            case NEXT_DAY -> "Overnight Priority Shipping (Next business day)";
            default -> throw new IllegalArgumentException("Unknown shipping type");
        };
    }
}

package cl.smartlogix.ms_envios.factory;

import org.springframework.stereotype.Component;

@Component
public class ShippingFactory {

    public enum ShippingType {
        STANDARD, EXPRESS, NEXT_DAY
    }

    public ShippingMethod createShippingMethod(ShippingType type) {
        switch (type) {
            case EXPRESS:
                return new ExpressShipping();
            case NEXT_DAY:
                return new SameDayShipping();
            case STANDARD:
            default:
                return new StandardShipping();
        }
    }
    
    // Método auxiliar para obtenerlo a partir de string
    public ShippingMethod createShippingMethodFromString(String typeString) {
        try {
            return createShippingMethod(ShippingType.valueOf(typeString.toUpperCase()));
        } catch (Exception e) {
            return new StandardShipping();
        }
    }
}

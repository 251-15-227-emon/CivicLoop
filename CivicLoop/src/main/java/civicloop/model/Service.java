package civicloop.model;

import java.io.Serializable;
import java.util.UUID;

public class Service implements Creditable, Serializable {
    private String serviceId;
    private String serviceType;
    private String providerId;
    private boolean isAvailable;

    public Service(String serviceType, String providerId) {
        this.serviceId = UUID.randomUUID().toString().substring(0, 8);
        this.serviceType = serviceType;
        this.providerId = providerId;
        this.isAvailable = true;
    }

    @Override
    public double getCreditRate() {
        return 1.0;
    }

    @Override
    public String getOfferType() {
        return "Service";
    }

    public String getServiceId() { return serviceId; }
    public String getServiceType() { return serviceType; }
    public String getProviderId() { return providerId; }
    public boolean isAvailable() { return isAvailable; }
    public void markBusy() { this.isAvailable = false; }
    public void markAvailable() { this.isAvailable = true; }
}
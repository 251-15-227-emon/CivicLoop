package civicloop.model;

import java.io.Serializable;

public class Service implements Creditable, Serializable {
    private String serviceId;
    private String serviceType;
    private String providerId;
    private boolean isAvailable;

    public Service(String serviceId, String serviceType, String providerId) {
        this.serviceId = serviceId;
        this.serviceType = serviceType;
        this.providerId = providerId;
        this.isAvailable = true;
    }

    @Override
    public double getCreditRate() { return 1.0; }
    @Override
    public String getOfferType() { return "Service"; }

    public String getServiceId() { return serviceId; }
    public String getServiceType() { return serviceType; }
    public String getProviderId() { return providerId; }
    public boolean isAvailable() { return isAvailable; }
    public void markBusy() { this.isAvailable = false; }
    public void markAvailable() { this.isAvailable = true; }
}
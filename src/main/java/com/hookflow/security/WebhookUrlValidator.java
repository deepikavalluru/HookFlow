package com.hookflow.security;

import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

@Component
public class WebhookUrlValidator {

    public void validate(String url) {

        try {
            URI uri = new URI(url);

            String scheme = uri.getScheme();

            if (scheme == null ||
                    !(scheme.equalsIgnoreCase("http")
                            || scheme.equalsIgnoreCase("https"))) {
                throw new IllegalArgumentException("Only HTTP and HTTPS URLs are allowed");
            }

            String host = uri.getHost();

            if (host == null || uri.getUserInfo() != null || uri.getFragment() != null) {
                throw new IllegalArgumentException("Invalid webhook URL");
            }

            if (host.equalsIgnoreCase("localhost")
                    || host.endsWith(".localhost")) {
                throw new IllegalArgumentException("Localhost URLs are not allowed");
            }

            InetAddress[] addresses = InetAddress.getAllByName(host);

            if (addresses.length == 0) {
                throw new IllegalArgumentException("Invalid hostname");
            }

            for (InetAddress address : addresses) {

                if (!isPublicAddress(address)) {
                    throw new IllegalArgumentException(
                            "Private or internal IP addresses are not allowed"
                    );
                }
            }

        } catch (UnknownHostException | java.net.URISyntaxException e) {
            throw new IllegalArgumentException("Invalid webhook URL");
        }
    }

    private boolean isPublicAddress(InetAddress address) {

        return !address.isAnyLocalAddress()
                && !address.isLoopbackAddress()
                && !address.isLinkLocalAddress()
                && !address.isSiteLocalAddress()
                && !address.isMulticastAddress();
    }
}
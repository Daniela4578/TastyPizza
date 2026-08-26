package services.interfaces;

import objects.Address;

import java.util.List;

public interface IAddressService {
    Address addAddress(Long userId, String name, double latitude, double longitude);

    List<Address> getAddresses(Long userId);

    void deleteAddress(Long addressId, Long userId);
}
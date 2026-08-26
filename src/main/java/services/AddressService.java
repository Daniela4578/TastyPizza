package services;

import objects.Address;
import repositories.interfaces.AddressRepository;
import services.interfaces.IAddressService;
import utilitis.ArgumentUtils;

import java.util.List;

public class AddressService implements IAddressService {

    private final AddressRepository addressRepository;

    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @Override
    public Address addAddress(Long userId, String name, double latitude, double longitude) {
        ArgumentUtils.requireNonBlank(name, "Address name");
        return addressRepository.save(Address.builder()
                .userId(userId).name(name.trim())
                .latitude(latitude).longitude(longitude).build());
    }

    @Override
    public List<Address> getAddresses(Long userId) {
        return addressRepository.findByUserId(userId);
    }

    @Override
    public void deleteAddress(Long addressId, Long userId) {
        Address address = addressRepository.findById(addressId).orElseThrow(() ->
                new IllegalArgumentException("Address not found: " + addressId));
        if (!address.getUserId().equals(userId))
            throw new IllegalArgumentException("You can only delete your own addresses");
        addressRepository.deleteById(addressId);
    }
}
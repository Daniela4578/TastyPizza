package repositories;

import db.DatabaseConnection;
import objects.Address;
import repositories.interfaces.AddressRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcAddressRepository implements AddressRepository {

    @Override
    public Address save(Address address) {
        String sql = "INSERT INTO addresses(user_id, name, latitude, longitude) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, address.getUserId());
            stmt.setString(2, address.getName());
            stmt.setDouble(3, address.getLatitude());
            stmt.setDouble(4, address.getLongitude());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return Address.builder()
                            .id(keys.getLong(1))
                            .userId(address.getUserId())
                            .name(address.getName())
                            .latitude(address.getLatitude())
                            .longitude(address.getLongitude())
                            .build();
                }
                throw new SQLException("Failed to get generated address ID");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save address", e);
        }
    }

    @Override
    public List<Address> findByUserId(Long userId) {
        String sql = "SELECT * FROM addresses WHERE user_id = ?";
        List<Address> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find addresses for user: " + userId, e);
        }
        return list;
    }

    @Override
    public Optional<Address> findById(Long id) {
        String sql = "SELECT * FROM addresses WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find address by ID: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM addresses WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete address: " + id, e);
        }
    }

    private Address mapRow(ResultSet rs) throws SQLException {
        return Address.builder()
                .id(rs.getLong("id"))
                .userId(rs.getLong("user_id"))
                .name(rs.getString("name"))
                .latitude(rs.getDouble("latitude"))
                .longitude(rs.getDouble("longitude"))
                .build();
    }
}
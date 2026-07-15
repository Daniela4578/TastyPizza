package server;

import exceptions.AccountNotActiveException;
import exceptions.EmailAlreadyExistsException;
import objects.*;
import services.*;

import java.io.*;
import java.math.BigDecimal;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ClientHandler implements Runnable {

    private static final BigDecimal DELIVERY_FEE = new BigDecimal("2.99");

    private final Socket           clientSocket;
    private final ServiceContainer services;
    private BufferedReader         in;
    private PrintWriter            out;

    public ClientHandler(Socket clientSocket, ServiceContainer services) {
        this.clientSocket = clientSocket;
        this.services     = services;
    }

    @Override
    public void run() {
        try {
            in  = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            out.println("================================");
            out.println("   WELCOME TO TASTY PIZZA!     ");
            out.println("================================");

            User loggedInUser = null;
            boolean running = true;

            while (running) {
                if (loggedInUser == null) {
                    printMainMenu();
                    String choice = in.readLine();
                    if (choice == null) break;
                    switch (choice.trim()) {
                        case "1" -> handleRegistration();
                        case "2" -> loggedInUser = handleLogin();
                        case "3" -> { out.println("Goodbye!"); running = false; }
                        default  -> out.println("Invalid choice.");
                    }
                } else {
                    switch (loggedInUser.getRole()) {
                        case CUSTOMER -> {
                            printCustomerMenu();
                            String choice = in.readLine();
                            if (choice == null) { running = false; break; }
                            switch (choice.trim()) {
                                case "1" -> handleBrowseMenu();
                                case "2" -> handlePlaceOrder(loggedInUser);
                                case "3" -> handleMyOrders(loggedInUser);
                                case "4" -> handleMyAddresses(loggedInUser);
                                case "5" -> handleDeleteAccount(loggedInUser);
                                case "6" -> { out.println("Logged out."); loggedInUser = null; }
                                default  -> out.println("Invalid choice.");
                            }
                        }
                        case EMPLOYEE -> {
                            printEmployeeMenu();
                            String choice = in.readLine();
                            if (choice == null) { running = false; break; }
                            switch (choice.trim()) {
                                case "1" -> handleViewMyShifts(loggedInUser);
                                case "2" -> handleViewMyDetails(loggedInUser);
                                case "3" -> handleViewPendingOrders();
                                case "4" -> handleProcessOrder(loggedInUser);
                                case "5" -> handleViewLowStock();
                                case "6" -> { out.println("Logged out."); loggedInUser = null; }
                                default  -> out.println("Invalid choice.");
                            }
                        }
                        case MANAGER -> {
                            printManagerMenu();
                            String choice = in.readLine();
                            if (choice == null) { running = false; break; }
                            switch (choice.trim()) {
                                case "1"  -> handleViewPendingEmployees();
                                case "2"  -> handleApproveEmployee();
                                case "3"  -> handleFireEmployee();
                                case "4"  -> handleAssignShift();
                                case "5"  -> handleViewTodaysShifts();
                                case "6"  -> handleUpdateSalary();
                                case "7"  -> handleManageProducts();
                                case "8"  -> handleManageStock();
                                case "9"  -> handleViewAllOrders();
                                case "10" -> { out.println("Logged out."); loggedInUser = null; }
                                default   -> out.println("Invalid choice.");
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    // ── Menus ──────────────────────────────────────────────────────────────

    private void printMainMenu() {
        out.println("\nMAIN MENU:");
        out.println("1 - Register");
        out.println("2 - Login");
        out.println("3 - Exit");
        out.println("Choose:");
    }

    private void printCustomerMenu() {
        out.println("\nCUSTOMER MENU:");
        out.println("1 - Browse menu");
        out.println("2 - Place order");
        out.println("3 - My orders");
        out.println("4 - My addresses");
        out.println("5 - Delete my account");
        out.println("6 - Logout");
        out.println("Choose:");
    }

    private void printEmployeeMenu() {
        out.println("\nEMPLOYEE MENU:");
        out.println("1 - My shifts");
        out.println("2 - My details");
        out.println("3 - View pending orders");
        out.println("4 - Process order");
        out.println("5 - View low stock");
        out.println("6 - Logout");
        out.println("Choose:");
    }

    private void printManagerMenu() {
        out.println("\nMANAGER MENU:");
        out.println("1  - View pending employees");
        out.println("2  - Approve employee");
        out.println("3  - Fire employee");
        out.println("4  - Assign shift");
        out.println("5  - Today's shifts");
        out.println("6  - Update salary");
        out.println("7  - Manage products");
        out.println("8  - Manage stock");
        out.println("9  - View all orders");
        out.println("10 - Logout");
        out.println("Choose:");
    }

    // ── Auth ───────────────────────────────────────────────────────────────

    private void handleRegistration() throws IOException {
        out.println("\nREGISTRATION:");

        String email     = readValidatedInput("Email:", services.getUserService()::validateEmail);
        String password  = readValidatedInput("Password (min 6 chars):", services.getUserService()::validatePassword);
        String firstName = readValidatedInput("First name:", services.getUserService()::validateName);
        String lastName  = readValidatedInput("Last name:", services.getUserService()::validateName);
        String phone     = readValidatedInput("Phone number:", services.getUserService()::validatePhoneNumber);
        LocalDate dob    = readValidatedDate("Date of birth (YYYY-MM-DD):");
        Role role        = readValidatedRole();

        while (true) {
            try {
                services.getUserService().validateAge(role, dob);
                break;
            } catch (IllegalArgumentException e) {
                out.println("Error: " + e.getMessage());
                dob = readValidatedDate("Date of birth (YYYY-MM-DD):");
            }
        }

        try {
            User user = services.getUserService().register(email, password, firstName, lastName, phone, dob, role);
            if (user.getStatus() == AccountStatus.PENDING) {
                out.println("\nRegistration submitted! Your account is pending manager approval.");
            } else {
                out.println("\nAccount created successfully!");
                out.println(user);
            }
        } catch (EmailAlreadyExistsException e) {
            out.println("\nRegistration failed: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            out.println("\nRegistration failed: " + e.getMessage());
        }
    }

    private User handleLogin() throws IOException {
        out.println("\nLOGIN:");
        String email    = readValidatedInput("Email:", services.getUserService()::validateEmail);
        String password = readValidatedInput("Password:", services.getUserService()::validatePassword);

        try {
            User user = services.getUserService().login(email, password);
            out.println("\nLogin successful! Welcome, " + user.getFullName() + "!");
            return user;
        } catch (AccountNotActiveException e) {
            // custom exception gives a specific clear message
            out.println("\n" + e.getMessage());
            return null;
        } catch (IllegalArgumentException e) {
            out.println("\nLogin failed: " + e.getMessage());
            return null;
        }
    }

    // ── Customer ───────────────────────────────────────────────────────────

    private void handleBrowseMenu() throws IOException {
        List<Category> categories = services.getProductService().getCategories();
        if (categories.isEmpty()) { out.println("\nNo categories available."); return; }

        out.println("\nMENU:");
        for (int i = 0; i < categories.size(); i++)
            out.println((i + 1) + " - " + categories.get(i).getName());
        out.println("0 - Back");
        out.println("Choose category:");

        String input = in.readLine();
        if (input == null || input.trim().equals("0")) return;

        int idx;
        try { idx = Integer.parseInt(input.trim()) - 1; }
        catch (NumberFormatException e) { out.println("Invalid choice."); return; }
        if (idx < 0 || idx >= categories.size()) { out.println("Invalid choice."); return; }

        List<Product> products = services.getProductService().getProductsByCategory(categories.get(idx).getId());
        if (products.isEmpty()) { out.println("No products in this category."); return; }

        out.println("\n--- " + categories.get(idx).getName().toUpperCase() + " ---");
        for (Product p : products) {
            List<ProductSize> sizes = services.getProductService().getSizesByProduct(p.getId());
            if (sizes.isEmpty()) {
                out.println(String.format("  [%d] %s - %.2f BGN", p.getId(), p.getName(), p.getPrice()));
            } else {
                out.println(String.format("  [%d] %s", p.getId(), p.getName()));
                for (ProductSize s : sizes)
                    out.println(String.format("       %s - %.2f BGN", s.getSizeLabel(), s.getPrice()));
            }
            if (p.getDescription() != null && !p.getDescription().isBlank())
                out.println("       " + p.getDescription());
        }
    }

    private void handlePlaceOrder(User customer) throws IOException {
        List<Address> addresses = services.getAddressService().getAddresses(customer.getId());
        if (addresses.isEmpty()) {
            out.println("\nYou need a delivery address first.");
            Address addr = handleAddNewAddress(customer);
            if (addr == null) return;
            addresses = services.getAddressService().getAddresses(customer.getId());
        }

        List<OrderItem> cart = new ArrayList<>();

        while (true) {
            List<Category> categories = services.getProductService().getCategories();
            out.println("\nADD ITEM — choose category (0 to checkout):");
            for (int i = 0; i < categories.size(); i++)
                out.println((i + 1) + " - " + categories.get(i).getName());
            out.println("0 - Checkout");
            out.println("Choose:");

            String catInput = in.readLine();
            if (catInput == null || catInput.trim().equals("0")) break;

            int catIdx;
            try { catIdx = Integer.parseInt(catInput.trim()) - 1; }
            catch (NumberFormatException e) { out.println("Invalid."); continue; }
            if (catIdx < 0 || catIdx >= categories.size()) { out.println("Invalid."); continue; }

            List<Product> products = services.getProductService().getProductsByCategory(categories.get(catIdx).getId());
            out.println("\nProducts:");
            for (int i = 0; i < products.size(); i++)
                out.println((i + 1) + " - " + products.get(i).getName() + " - " + products.get(i).getPrice() + " BGN");
            out.println("0 - Back");
            out.println("Choose:");

            String prodInput = in.readLine();
            if (prodInput == null || prodInput.trim().equals("0")) continue;

            int prodIdx;
            try { prodIdx = Integer.parseInt(prodInput.trim()) - 1; }
            catch (NumberFormatException e) { out.println("Invalid."); continue; }
            if (prodIdx < 0 || prodIdx >= products.size()) { out.println("Invalid."); continue; }

            Product product = products.get(prodIdx);
            List<ProductSize> sizes = services.getProductService().getSizesByProduct(product.getId());

            Long sizeId = null;
            String sizeName = null;
            BigDecimal unitPrice = product.getPrice();

            if (!sizes.isEmpty()) {
                out.println("\nChoose size:");
                for (int i = 0; i < sizes.size(); i++)
                    out.println((i + 1) + " - " + sizes.get(i).getSizeLabel() + " - " + sizes.get(i).getPrice() + " BGN");
                out.println("Choose:");
                String sizeInput = in.readLine();
                int sizeIdx;
                try { sizeIdx = Integer.parseInt(sizeInput.trim()) - 1; }
                catch (NumberFormatException e) { out.println("Invalid."); continue; }
                if (sizeIdx < 0 || sizeIdx >= sizes.size()) { out.println("Invalid."); continue; }
                sizeId    = sizes.get(sizeIdx).getId();
                sizeName  = sizes.get(sizeIdx).getSizeLabel();
                unitPrice = sizes.get(sizeIdx).getPrice();
            }

            out.println("Quantity:");
            int qty;
            try { qty = Integer.parseInt(in.readLine().trim()); if (qty <= 0) throw new NumberFormatException(); }
            catch (NumberFormatException e) { out.println("Invalid quantity."); continue; }

            out.println("Special instructions (Enter to skip):");
            String instructions = in.readLine();
            if (instructions != null && instructions.isBlank()) instructions = null;

            cart.add(OrderItem.builder()
                    .productId(product.getId()).productName(product.getName())
                    .productSizeId(sizeId).sizeName(sizeName)
                    .quantity(qty).unitPrice(unitPrice)
                    .specialInstructions(instructions).build());
            out.println("Added to cart.");
        }

        if (cart.isEmpty()) { out.println("Cart is empty. Order cancelled."); return; }

        out.println("\nYOUR ORDER:");
        BigDecimal itemsTotal = BigDecimal.ZERO;
        for (OrderItem item : cart) { out.println(item); itemsTotal = itemsTotal.add(item.getSubtotal()); }
        out.println(String.format("Items:    %.2f BGN", itemsTotal));
        out.println(String.format("Delivery: %.2f BGN", DELIVERY_FEE));
        out.println(String.format("TOTAL:    %.2f BGN", itemsTotal.add(DELIVERY_FEE)));

        out.println("\nDELIVERY ADDRESS:");
        for (int i = 0; i < addresses.size(); i++)
            out.println((i + 1) + " - " + addresses.get(i).getName());
        out.println((addresses.size() + 1) + " - Add new address");
        out.println("Choose:");

        String addrInput = in.readLine();
        int addrIdx;
        try { addrIdx = Integer.parseInt(addrInput.trim()) - 1; }
        catch (NumberFormatException e) { out.println("Invalid. Order cancelled."); return; }

        Long addressId;
        if (addrIdx == addresses.size()) {
            Address newAddr = handleAddNewAddress(customer);
            if (newAddr == null) { out.println("Order cancelled."); return; }
            addressId = newAddr.getId();
        } else if (addrIdx >= 0 && addrIdx < addresses.size()) {
            addressId = addresses.get(addrIdx).getId();
        } else {
            out.println("Invalid. Order cancelled.");
            return;
        }

        out.println("Confirm order? (yes/no):");
        String confirm = in.readLine();
        if (confirm == null || !confirm.trim().equalsIgnoreCase("yes")) { out.println("Order cancelled."); return; }

        try {
            Order order = services.getOrderService().placeOrder(customer.getId(), addressId, cart);
            out.println("\nOrder placed! Order #" + order.getId());
        } catch (Exception e) {
            out.println("\nFailed to place order: " + e.getMessage());
        }
    }

    private void handleMyOrders(User customer) {
        List<Order> orders = services.getOrderService().getMyOrders(customer.getId());
        if (orders.isEmpty()) { out.println("\nNo orders yet."); return; }
        out.println("\nMY ORDERS:");
        orders.forEach(o -> { out.println(o); out.println("---"); });
    }

    private void handleMyAddresses(User customer) throws IOException {
        List<Address> addresses = services.getAddressService().getAddresses(customer.getId());
        out.println("\nMY ADDRESSES:");
        if (addresses.isEmpty()) out.println("No addresses saved.");
        else addresses.forEach(a -> out.println(a));
        out.println("\n1 - Add new address");
        out.println("0 - Back");
        out.println("Choose:");
        String choice = in.readLine();
        if ("1".equals(choice != null ? choice.trim() : "")) handleAddNewAddress(customer);
    }

    private Address handleAddNewAddress(User customer) throws IOException {
        out.println("\nADD ADDRESS:");
        out.println("Name (e.g. Home, Work):");
        String name = in.readLine();
        if (name == null || name.isBlank()) { out.println("Cancelled."); return null; }

        double lat, lng;
        try {
            out.println("Latitude:");
            lat = Double.parseDouble(in.readLine().trim());
            out.println("Longitude:");
            lng = Double.parseDouble(in.readLine().trim());
        } catch (NumberFormatException e) {
            out.println("Invalid coordinates. Cancelled.");
            return null;
        }

        try {
            Address address = services.getAddressService().addAddress(customer.getId(), name.trim(), lat, lng);
            out.println("Address saved: " + address.getName());
            return address;
        } catch (Exception e) {
            out.println("Failed to save address: " + e.getMessage());
            return null;
        }
    }

    private void handleDeleteAccount(User user) throws IOException {
        out.println("\nAre you sure? This cannot be undone. (yes/no):");
        String confirm = in.readLine();
        if (confirm != null && confirm.trim().equalsIgnoreCase("yes")) {
            services.getUserService().deactivateAccount(user.getId());
            out.println("Account deactivated. Goodbye!");
        } else {
            out.println("Cancelled.");
        }
    }

    // ── Employee ───────────────────────────────────────────────────────────

    private void handleViewMyShifts(User employee) {
        List<Shift> shifts = services.getEmployeeService().getShiftsForEmployee(employee.getId());
        if (shifts.isEmpty()) { out.println("\nNo shifts assigned."); return; }
        out.println("\nMY SHIFTS:");
        shifts.forEach(s -> out.println(s));
    }

    private void handleViewMyDetails(User employee) {
        services.getEmployeeService().getEmployeeDetails(employee.getId())
                .ifPresentOrElse(
                        d -> out.println("\n" + d),
                        () -> out.println("\nNo details on file yet.")
                );
    }

    private void handleViewPendingOrders() {
        List<Order> orders = services.getOrderService().getPendingOrders();
        if (orders.isEmpty()) { out.println("\nNo pending orders."); return; }
        out.println("\nPENDING ORDERS:");
        orders.forEach(o -> { out.println(o); out.println("---"); });
    }

    private void handleProcessOrder(User employee) throws IOException {
        out.println("\nPROCESS ORDER:");
        out.println("Order ID:");
        Long orderId = readLong();
        if (orderId == null) return;
        out.println("Estimated delivery time (minutes):");
        int minutes;
        try { minutes = Integer.parseInt(in.readLine().trim()); }
        catch (NumberFormatException e) { out.println("Invalid time."); return; }
        try {
            services.getOrderService().processOrder(orderId, employee.getId(), minutes);
            out.println("Order #" + orderId + " is now PROCESSING. ETA: " + minutes + " min.");
        } catch (Exception e) {
            out.println("Error: " + e.getMessage());
        }
    }

    private void handleViewLowStock() {
        List<Ingredient> low = services.getIngredientService().getLowStockIngredients();
        if (low.isEmpty()) { out.println("\nAll ingredients sufficiently stocked."); return; }
        out.println("\nLOW STOCK ALERT:");
        low.forEach(i -> out.println(i));
    }

    // ── Manager ────────────────────────────────────────────────────────────

    private void handleViewPendingEmployees() {
        List<User> pending = services.getEmployeeService().getPendingEmployees();
        if (pending.isEmpty()) { out.println("\nNo pending employees."); return; }
        out.println("\nPENDING EMPLOYEES:");
        pending.forEach(u -> out.println(String.format("[%d] %s — %s", u.getId(), u.getFullName(), u.getEmail())));
    }

    private void handleApproveEmployee() throws IOException {
        out.println("\nAPPROVE EMPLOYEE:");
        handleViewPendingEmployees();
        out.println("Enter employee user ID:");
        Long userId = readLong();
        if (userId == null) return;
        out.println("Enter salary:");
        BigDecimal salary = readBigDecimal();
        if (salary == null) return;
        LocalDate hireDate = readValidatedDate("Hire date (YYYY-MM-DD):");
        try {
            EmployeeDetails details = services.getEmployeeService().approveEmployee(userId, salary, hireDate);
            out.println("Employee approved! " + details);
        } catch (Exception e) { out.println("Error: " + e.getMessage()); }
    }

    private void handleFireEmployee() throws IOException {
        out.println("\nFIRE EMPLOYEE:");
        out.println("Employee user ID:");
        Long userId = readLong();
        if (userId == null) return;
        out.println("Are you sure? (yes/no):");
        String confirm = in.readLine();
        if (confirm != null && confirm.trim().equalsIgnoreCase("yes")) {
            try { services.getEmployeeService().fireEmployee(userId); out.println("Employee terminated."); }
            catch (Exception e) { out.println("Error: " + e.getMessage()); }
        } else { out.println("Cancelled."); }
    }

    private void handleAssignShift() throws IOException {
        out.println("\nASSIGN SHIFT:");
        out.println("Employee user ID:");
        Long employeeId = readLong();
        if (employeeId == null) return;
        out.println("Employee name:");
        String employeeName = in.readLine();
        LocalDate date      = readValidatedDate("Shift date (YYYY-MM-DD):");
        LocalTime startTime = readValidatedTime("Start time (HH:MM):");
        LocalTime endTime   = readValidatedTime("End time (HH:MM):");
        try {
            Shift shift = services.getEmployeeService().assignShift(employeeId, employeeName, date, startTime, endTime);
            out.println("Shift assigned: " + shift);
        } catch (Exception e) { out.println("Error: " + e.getMessage()); }
    }

    private void handleViewTodaysShifts() {
        List<Shift> shifts = services.getEmployeeService().getTodaysShifts();
        if (shifts.isEmpty()) { out.println("\nNo shifts today."); return; }
        out.println("\nTODAY'S SHIFTS:");
        shifts.forEach(s -> out.println(s));
    }

    private void handleUpdateSalary() throws IOException {
        out.println("\nUPDATE SALARY:");
        out.println("Employee user ID:");
        Long userId = readLong();
        if (userId == null) return;
        out.println("New salary:");
        BigDecimal salary = readBigDecimal();
        if (salary == null) return;
        try { services.getEmployeeService().updateSalary(userId, salary); out.println("Salary updated."); }
        catch (Exception e) { out.println("Error: " + e.getMessage()); }
    }

    private void handleManageProducts() throws IOException {
        out.println("\nMANAGE PRODUCTS:");
        out.println("1 - Add product");
        out.println("2 - Deactivate product");
        out.println("0 - Back");
        out.println("Choose:");
        String choice = in.readLine();
        if (choice == null) return;
        switch (choice.trim()) {
            case "1" -> {
                List<Category> cats = services.getProductService().getCategories();
                for (int i = 0; i < cats.size(); i++) out.println((i + 1) + " - " + cats.get(i).getName());
                out.println("Choose category:");
                int catIdx;
                try { catIdx = Integer.parseInt(in.readLine().trim()) - 1; }
                catch (NumberFormatException e) { out.println("Invalid."); return; }
                if (catIdx < 0 || catIdx >= cats.size()) { out.println("Invalid."); return; }
                out.println("Product name:");
                String name = in.readLine();
                out.println("Description (Enter to skip):");
                String desc = in.readLine();
                if (desc != null && desc.isBlank()) desc = null;
                out.println("Price:");
                BigDecimal price = readBigDecimal();
                if (price == null) return;
                try {
                    Product p = services.getProductService().addProduct(name, desc, price, cats.get(catIdx).getId());
                    out.println("Product added: " + p);
                } catch (Exception e) { out.println("Error: " + e.getMessage()); }
            }
            case "2" -> {
                out.println("Product ID:");
                Long pid = readLong();
                if (pid == null) return;
                try { services.getProductService().deactivateProduct(pid); out.println("Product deactivated."); }
                catch (Exception e) { out.println("Error: " + e.getMessage()); }
            }
        }
    }

    private void handleManageStock() throws IOException {
        out.println("\nMANAGE STOCK:");
        out.println("1 - View all ingredients");
        out.println("2 - Restock ingredient");
        out.println("3 - Set minimum stock");
        out.println("0 - Back");
        out.println("Choose:");
        String choice = in.readLine();
        if (choice == null) return;
        switch (choice.trim()) {
            case "1" -> {
                List<Ingredient> all = services.getIngredientService().getAllIngredients();
                if (all.isEmpty()) out.println("No ingredients.");
                else all.forEach(i -> out.println(i));
            }
            case "2" -> {
                out.println("Ingredient ID:");
                Long id = readLong();
                if (id == null) return;
                out.println("Amount to add:");
                BigDecimal amount = readBigDecimal();
                if (amount == null) return;
                try { services.getIngredientService().restock(id, amount); out.println("Restocked."); }
                catch (Exception e) { out.println("Error: " + e.getMessage()); }
            }
            case "3" -> {
                out.println("Ingredient ID:");
                Long id = readLong();
                if (id == null) return;
                out.println("New minimum stock:");
                BigDecimal min = readBigDecimal();
                if (min == null) return;
                try { services.getIngredientService().setMinimumStock(id, min); out.println("Minimum stock updated."); }
                catch (Exception e) { out.println("Error: " + e.getMessage()); }
            }
        }
    }

    private void handleViewAllOrders() throws IOException {
        List<Order> orders = services.getOrderService().getPendingOrders();
        if (orders.isEmpty()) { out.println("\nNo pending orders."); return; }
        out.println("\nPENDING ORDERS:");
        orders.forEach(o -> { out.println(o); out.println("---"); });
        out.println("\n1 - Mark as delivered");
        out.println("2 - Cancel order");
        out.println("0 - Back");
        out.println("Choose:");
        String choice = in.readLine();
        if (choice == null || choice.trim().equals("0")) return;
        out.println("Order ID:");
        Long orderId = readLong();
        if (orderId == null) return;
        try {
            if ("1".equals(choice.trim())) {
                services.getOrderService().deliverOrder(orderId);
                out.println("Order #" + orderId + " marked as DELIVERED.");
            } else if ("2".equals(choice.trim())) {
                services.getOrderService().cancelOrder(orderId);
                out.println("Order #" + orderId + " CANCELLED.");
            }
        } catch (Exception e) { out.println("Error: " + e.getMessage()); }
    }

    // ── Input helpers ──────────────────────────────────────────────────────

    private String readInput(String prompt) throws IOException {
        out.println(prompt);
        return in.readLine();
    }

    private String readValidatedInput(String prompt, Consumer<String> validator) throws IOException {
        while (true) {
            String input = readInput(prompt);
            if (input == null) throw new IOException("Client disconnected");
            try { validator.accept(input); return input; }
            catch (IllegalArgumentException e) { out.println("Error: " + e.getMessage() + " Try again."); }
        }
    }

    private LocalDate readValidatedDate(String prompt) throws IOException {
        while (true) {
            out.println(prompt);
            String input = in.readLine();
            if (input == null) throw new IOException("Client disconnected");
            try { return LocalDate.parse(input.trim(), DateTimeFormatter.ISO_LOCAL_DATE); }
            catch (DateTimeParseException e) { out.println("Invalid date. Use YYYY-MM-DD. Try again."); }
        }
    }

    private LocalTime readValidatedTime(String prompt) throws IOException {
        while (true) {
            out.println(prompt);
            String input = in.readLine();
            if (input == null) throw new IOException("Client disconnected");
            try { return LocalTime.parse(input.trim(), DateTimeFormatter.ofPattern("HH:mm")); }
            catch (DateTimeParseException e) { out.println("Invalid time. Use HH:MM. Try again."); }
        }
    }

    private Role readValidatedRole() throws IOException {
        while (true) {
            out.println("Role:\n  1. Customer\n  2. Employee\nChoose:");
            String choice = in.readLine();
            if (choice == null) throw new IOException("Client disconnected");
            switch (choice.trim()) {
                case "1" -> { return Role.CUSTOMER; }
                case "2" -> { return Role.EMPLOYEE; }
                default  -> out.println("Invalid. Enter 1 or 2.");
            }
        }
    }

    private Long readLong() throws IOException {
        String input = in.readLine();
        try { return Long.parseLong(input.trim()); }
        catch (NumberFormatException e) { out.println("Invalid ID."); return null; }
    }

    private BigDecimal readBigDecimal() throws IOException {
        String input = in.readLine();
        try { return new BigDecimal(input.trim()); }
        catch (NumberFormatException e) { out.println("Invalid number."); return null; }
    }

    private void closeConnection() {
        try {
            if (in != null)           in.close();
            if (out != null)          out.close();
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }
}
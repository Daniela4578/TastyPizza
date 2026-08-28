package config;

import repositories.*;
import repositories.interfaces.*;
import services.*;
import services.interfaces.*;


public final class AppConfig {

    private AppConfig() {
        throw new AssertionError("Static factory class");
    }

    public static ServiceContainer createServices() {

        // repositories
        UserRepository userRepository = new JdbcUserRepository();
        EmployeeDetailsRepository employeeDetailsRepository = new JdbcEmployeeDetailsRepository();
        ShiftRepository shiftRepository = new JdbcShiftRepository();
        AddressRepository addressRepository = new JdbcAddressRepository();
        CategoryRepository categoryRepository = new JdbcCategoryRepository();
        ProductRepository productRepository = new JdbcProductRepository();
        IngredientRepository ingredientRepository = new JdbcIngredientRepository();
        OrderRepository orderRepository = new JdbcOrderRepository();
        PaymentRepository paymentRepository = new JdbcPaymentRepository();
        OrderStatusHistoryRepository historyRepository = new JdbcOrderStatusHistoryRepository();

        // services
        IUserService userService = new UserService(userRepository);
        IEmployeeService employeeService = new EmployeeService(
                employeeDetailsRepository, shiftRepository, userRepository);
        IAddressService addressService = new AddressService(addressRepository);
        IProductService productService = new ProductService(productRepository, categoryRepository);
        IIngredientService ingredientService = new IngredientService(ingredientRepository, productService);
        IPaymentService paymentService = new PaymentService(paymentRepository);

        IOrderService orderService = new OrderService(
                orderRepository,
                ingredientRepository,
                paymentRepository,
                productService);

        IOrderHistoryService orderHistoryService = new OrderHistoryService(historyRepository);

        return new ServiceContainer(
                userService, employeeService, addressService,
                productService, orderService, ingredientService,
                paymentService, orderHistoryService);
    }
}
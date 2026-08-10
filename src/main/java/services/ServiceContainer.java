package services;

public class ServiceContainer {

    private final UserService         userService;
    private final EmployeeService     employeeService;
    private final AddressService      addressService;
    private final ProductService      productService;
    private final OrderService        orderService;
    private final IngredientService   ingredientService;
    private final PaymentService      paymentService;
    private final OrderHistoryService orderHistoryService;

    public ServiceContainer(UserService         userService,
                            EmployeeService     employeeService,
                            AddressService      addressService,
                            ProductService      productService,
                            OrderService        orderService,
                            IngredientService   ingredientService,
                            PaymentService      paymentService,
                            OrderHistoryService orderHistoryService) {
        this.userService         = userService;
        this.employeeService     = employeeService;
        this.addressService      = addressService;
        this.productService      = productService;
        this.orderService        = orderService;
        this.ingredientService   = ingredientService;
        this.paymentService      = paymentService;
        this.orderHistoryService = orderHistoryService;
    }

    public UserService         getUserService()         { return userService; }
    public EmployeeService     getEmployeeService()     { return employeeService; }
    public AddressService      getAddressService()      { return addressService; }
    public ProductService      getProductService()      { return productService; }
    public OrderService        getOrderService()        { return orderService; }
    public IngredientService   getIngredientService()   { return ingredientService; }
    public PaymentService      getPaymentService()      { return paymentService; }
    public OrderHistoryService getOrderHistoryService() { return orderHistoryService; }
}
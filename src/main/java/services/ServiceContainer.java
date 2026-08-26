package services;

import services.interfaces.*;

public class ServiceContainer {

    private final IUserService userService;
    private final IEmployeeService employeeService;
    private final IAddressService addressService;
    private final IProductService productService;
    private final IOrderService orderService;
    private final IIngredientService ingredientService;
    private final IPaymentService paymentService;
    private final IOrderHistoryService orderHistoryService;

    public ServiceContainer(IUserService userService,
                            IEmployeeService employeeService,
                            IAddressService addressService,
                            IProductService productService,
                            IOrderService orderService,
                            IIngredientService ingredientService,
                            IPaymentService paymentService,
                            IOrderHistoryService orderHistoryService) {
        this.userService = userService;
        this.employeeService = employeeService;
        this.addressService = addressService;
        this.productService = productService;
        this.orderService = orderService;
        this.ingredientService = ingredientService;
        this.paymentService = paymentService;
        this.orderHistoryService = orderHistoryService;
    }

    public IUserService getUserService() {
        return userService;
    }

    public IEmployeeService getEmployeeService() {
        return employeeService;
    }

    public IAddressService getAddressService() {
        return addressService;
    }

    public IProductService getProductService() {
        return productService;
    }

    public IOrderService getOrderService() {
        return orderService;
    }

    public IIngredientService getIngredientService() {
        return ingredientService;
    }

    public IPaymentService getPaymentService() {
        return paymentService;
    }

    public IOrderHistoryService getOrderHistoryService() {
        return orderHistoryService;
    }
}
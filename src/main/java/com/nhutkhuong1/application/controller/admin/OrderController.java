package com.nhutkhuong1.application.controller.admin;

import com.nhutkhuong1.application.config.Contant;
import com.nhutkhuong1.application.entity.Order;
import com.nhutkhuong1.application.entity.Promotion;
import com.nhutkhuong1.application.entity.User;
import com.nhutkhuong1.application.exception.BadRequestException;
import com.nhutkhuong1.application.model.dto.OrderDetailDTO;
import com.nhutkhuong1.application.model.dto.OrderInfoDTO;
import com.nhutkhuong1.application.model.dto.ShortProductInfoDTO;
import com.nhutkhuong1.application.model.request.CreateOrderRequest;
import com.nhutkhuong1.application.model.request.UpdateDetailOrder;
import com.nhutkhuong1.application.model.request.UpdateStatusOrderRequest;
import com.nhutkhuong1.application.security.CustomUserDetails;
import com.nhutkhuong1.application.service.OrderService;
import com.nhutkhuong1.application.service.ProductService;
import com.nhutkhuong1.application.service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Controller
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    @Autowired
    private PromotionService promotionService;

    @GetMapping("/admin/orders")
    public String getListOrderPage(Model model,
                                   @RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "", required = false) String id,
                                   @RequestParam(defaultValue = "", required = false) String name,
                                   @RequestParam(defaultValue = "", required = false) String phone,
                                   @RequestParam(defaultValue = "", required = false) String status,
                                   @RequestParam(defaultValue = "", required = false) String product) {

        //Lấy danh sách sản phẩm
        List<ShortProductInfoDTO> productList = productService.getListProduct();
        model.addAttribute("productList", productList);


        //Lấy danh sách đơn hàng
        Page<Order> orderPage = orderService.adminGetListOrders(id, name, phone, status, product, page);
        model.addAttribute("orderPage", orderPage.getContent());
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("currentPage", orderPage.getPageable().getPageNumber() + 1);

        return "admin/order/list";
    }

    @GetMapping("/admin/orders/create")
    public String createOrderPage(Model model) {

        //Get list product
        List<ShortProductInfoDTO> products = productService.getAvailableProducts();
        model.addAttribute("products", products);

        // Get list size
        model.addAttribute("sizeVn", Contant.SIZE_VN);

//        //Get list valid promotion
        List<Promotion> promotions = promotionService.getAllValidPromotion();
        model.addAttribute("promotions", promotions);
        return "admin/order/create";
    }

    @PostMapping("/api/admin/orders")
    public ResponseEntity<Object> createOrder(@Valid @RequestBody CreateOrderRequest createOrderRequest) {
        User user = ((CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
        Order order = orderService.createOrder(createOrderRequest, user.getId());
        return ResponseEntity.ok(order);
    }

    @GetMapping("/admin/orders/update/{id}")
    public String updateOrderPage(Model model, @PathVariable long id) {

        Order order = orderService.findOrderById(id);
        model.addAttribute("order", order);

        if (order.getStatus() == Contant.ORDER_STATUS) {
            // Get list product to select
            List<ShortProductInfoDTO> products = productService.getAvailableProducts();
            model.addAttribute("products", products);

            // Get list valid promotion
            List<Promotion> promotions = promotionService.getAllValidPromotion();
            model.addAttribute("promotions", promotions);
            if (order.getPromotion() != null) {
                boolean validPromotion = false;
                for (Promotion promotion : promotions) {
                    if (promotion.getCouponCode().equals(order.getPromotion().getCouponCode())) {
                        validPromotion = true;
                        break;
                    }
                }
                if (!validPromotion) {
                    promotions.add(new Promotion(order.getPromotion()));
                }
            }

            // Check size available
            boolean sizeIsAvailable = productService.checkProductSizeAvailable(order.getProduct().getId(), order.getSize());
            model.addAttribute("sizeIsAvailable", sizeIsAvailable);
        }

        return "admin/order/edit";
    }

    @PutMapping("/api/admin/orders/update-detail/{id}")
    public ResponseEntity<Object> updateDetailOrder(@Valid @RequestBody UpdateDetailOrder detailOrder, @PathVariable long id) {
        User user = ((CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
        orderService.updateDetailOrder(detailOrder, id, user.getId());
        return ResponseEntity.ok("Cập nhật thành công");
    }

    @PutMapping("/api/admin/orders/update-status/{id}")
    public ResponseEntity<Object> updateStatusOrder(@Valid @RequestBody UpdateStatusOrderRequest updateStatusOrderRequest, @PathVariable long id) {
        User user = ((CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
        orderService.updateStatusOrder(updateStatusOrderRequest, id, user.getId());
        return ResponseEntity.ok("Cập nhật trạng thái thành công");
    }

    @GetMapping("/tai-khoan/lich-su-giao-dich")
    public String getOrderHistoryPage(Model model){

        //Get list order pending
        User user =((CustomUserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
        List<OrderInfoDTO> orderList = orderService.getListOrderOfPersonByStatus(Contant.ORDER_STATUS,user.getId());
        model.addAttribute("orderList",orderList);

        return "shop/order_history";
    }

    @GetMapping("/api/get-order-list")
    public ResponseEntity<Object> getListOrderByStatus(@RequestParam int status) {
        // Validate status
        if (!Contant.LIST_ORDER_STATUS.contains(status)) {
            throw new BadRequestException("Trạng thái đơn hàng không hợp lệ");
        }

        User user = ((CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
        List<OrderInfoDTO> orders = orderService.getListOrderOfPersonByStatus(status, user.getId());

        return ResponseEntity.ok(orders);
    }

    @GetMapping("/tai-khoan/lich-su-giao-dich/{id}")
    public String getDetailOrderPage(Model model, @PathVariable int id) {
        User user = ((CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();

        OrderDetailDTO order = orderService.userGetDetailById(id, user.getId());
        if (order == null) {
            return "error/404";
        }
        model.addAttribute("order", order);

        if (order.getStatus() == Contant.ORDER_STATUS) {
            model.addAttribute("canCancel", true);
        } else {
            model.addAttribute("canCancel", false);
        }

        return "shop/order-detail";
    }

    @PostMapping("/api/cancel-order/{id}")
    public ResponseEntity<Object> cancelOrder(@PathVariable long id) {
        User user = ((CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();

        orderService.userCancelOrder(id, user.getId());

        return ResponseEntity.ok("Hủy đơn hàng thành công");
    }

}

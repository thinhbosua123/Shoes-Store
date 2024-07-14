package com.nhutkhuong1.application.controller.admin;

import com.nhutkhuong1.application.entity.Promotion;
import com.nhutkhuong1.application.model.request.CreatePromotionRequest;
import com.nhutkhuong1.application.service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Controller
public class PromotionController {

    @Autowired
    private PromotionService promotionService;

    @GetMapping("/admin/promotions")
    public String getListPromotionPages(Model model,
                                        @RequestParam(defaultValue = "1") Integer page,
                                        @RequestParam(defaultValue = "", required = false) String code,
                                        @RequestParam(defaultValue = "", required = false) String name,
                                        @RequestParam(defaultValue = "", required = false) String publish,
                                        @RequestParam(defaultValue = "", required = false) String active) {


        Page<Promotion> promotionPage = promotionService.adminGetListPromotion(code, name, publish, active, page);
        model.addAttribute("promotionPage", promotionPage);
        model.addAttribute("totalPages", promotionPage.getTotalPages());
        model.addAttribute("currentPage", promotionPage.getPageable().getPageNumber() + 1);
        return "admin/promotion/list";
    }

    @GetMapping("/admin/promotions/create")
    public String createPromotionPage(Model model) {
        return "admin/promotion/create";
    }

    @PostMapping("/api/admin/promotions")
    public ResponseEntity<Object> createPromotion(@Valid @RequestBody CreatePromotionRequest createPromotionRequest) {
        Promotion promotion = promotionService.createPromotion(createPromotionRequest);
        return ResponseEntity.ok(promotion.getId());
    }

    @GetMapping("/admin/promotions/update/{id}")
    public String updatePromotionPage(Model model, @PathVariable long id) {
        Promotion promotion = promotionService.findPromotionById(id);
        model.addAttribute("promotion", promotion);
        return "admin/promotion/edit";
    }

    @PutMapping("/api/admin/promotions/{id}")
    public ResponseEntity<Object> updatePromotion(@Valid @RequestBody CreatePromotionRequest createPromotionRequest, @PathVariable long id) {
        promotionService.updatePromotion(createPromotionRequest, id);
        return ResponseEntity.ok("Cập nhật thành công");
    }

    @DeleteMapping("/api/admin/promotions/{id}")
    public ResponseEntity<Object> deletePromotion(@PathVariable long id) {
        promotionService.deletePromotion(id);
        return ResponseEntity.ok("Xóa khuyến mại thành công");
    }
}

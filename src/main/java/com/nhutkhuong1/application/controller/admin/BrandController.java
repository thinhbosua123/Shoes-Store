package com.nhutkhuong1.application.controller.admin;

import com.nhutkhuong1.application.entity.Brand;
import com.nhutkhuong1.application.entity.User;
import com.nhutkhuong1.application.model.mapper.BrandMapper;
import com.nhutkhuong1.application.model.request.CreateBrandRequest;
import com.nhutkhuong1.application.security.CustomUserDetails;
import com.nhutkhuong1.application.service.BrandService;
import com.nhutkhuong1.application.service.ImageService;
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
public class BrandController {

    @Autowired
    private BrandService brandService;

    @Autowired
    private ImageService imageService;

    @GetMapping("/admin/brands")
    public String homePage(Model model,
                           @RequestParam(defaultValue = "", required = false) String id,
                           @RequestParam(defaultValue = "", required = false) String name,
                           @RequestParam(defaultValue = "", required = false) String status,
                           @RequestParam(defaultValue = "1", required = false) Integer page) {

        //Lấy tất cả các anh của user upload
        User user = ((CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
        List<String> images = imageService.getListImageOfUser(user.getId());
        model.addAttribute("images", images);

        Page<Brand> brands = brandService.adminGetListBrands(id, name, status, page);
        model.addAttribute("brands", brands.getContent());
        model.addAttribute("totalPages", brands.getTotalPages());
        model.addAttribute("currentPage", brands.getPageable().getPageNumber() + 1);
        return "admin/brand/list";
    }

    @PostMapping("/api/admin/brands")
    public ResponseEntity<Object> createBrand(@Valid @RequestBody CreateBrandRequest createBrandRequest) {
        Brand brand = brandService.createBrand(createBrandRequest);
        return ResponseEntity.ok(BrandMapper.toBrandDTO(brand));
    }

    @PutMapping("/api/admin/brands/{id}")
    public ResponseEntity<Object> updateBrand(@Valid @RequestBody CreateBrandRequest createBrandRequest, @PathVariable long id) {
        brandService.updateBrand(createBrandRequest, id);
        return ResponseEntity.ok("Sửa nhãn hiệu thành công!");
    }

    @DeleteMapping("/api/admin/brands/{id}")
    public ResponseEntity<Object> deleteBrand(@PathVariable long id) {
        brandService.deleteBrand(id);
        return ResponseEntity.ok("Xóa nhãn hiệu thành công!");
    }
    @GetMapping("/api/admin/brands/{id}")
    public ResponseEntity<Object> getBrandById(@PathVariable long id){
        Brand brand = brandService.getBrandById(id);
        return ResponseEntity.ok(brand);
    }
}

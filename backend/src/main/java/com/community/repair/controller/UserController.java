package com.community.repair.controller;

import com.community.repair.common.ApiResponse;
import com.community.repair.dto.WorkerVO;
import com.community.repair.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** 派单候选维修工，按当前在手工单数升序 */
    @GetMapping("/workers")
    @PreAuthorize("hasAnyRole('CS','COMMUNITY')")
    public ApiResponse<List<WorkerVO>> workers() {
        return ApiResponse.ok(userService.listWorkers());
    }
}

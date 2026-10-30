package com.community.repair.controller;

import com.community.repair.common.ApiResponse;
import com.community.repair.common.SecurityUtils;
import com.community.repair.dto.AssignTicketRequest;
import com.community.repair.dto.CreateTicketRequest;
import com.community.repair.dto.FinishTicketRequest;
import com.community.repair.dto.PageResponse;
import com.community.repair.dto.ReasonRequest;
import com.community.repair.dto.RemarkRequest;
import com.community.repair.dto.TicketDetailResponse;
import com.community.repair.dto.TicketQuery;
import com.community.repair.entity.Ticket;
import com.community.repair.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    /** 建单：业主扫码报修，或客服代报修 */
    @PostMapping
    public ApiResponse<Ticket> create(@Valid @RequestBody CreateTicketRequest request) {
        return ApiResponse.ok(ticketService.create(request, SecurityUtils.current()));
    }

    /** 工单列表，按角色自动限定可见范围 */
    @GetMapping
    public ApiResponse<PageResponse<Ticket>> list(@ModelAttribute TicketQuery query) {
        return ApiResponse.ok(ticketService.list(query, SecurityUtils.current()));
    }

    /** 工单详情 + 状态时间线 */
    @GetMapping("/{id}")
    public ApiResponse<TicketDetailResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok(ticketService.detail(id, SecurityUtils.current()));
    }

    @PutMapping("/{id}/assign")
    public ApiResponse<Ticket> assign(@PathVariable Long id, @Valid @RequestBody AssignTicketRequest request) {
        return ApiResponse.ok(ticketService.assign(id, request, SecurityUtils.current()));
    }

    @PutMapping("/{id}/accept")
    public ApiResponse<Ticket> accept(@PathVariable Long id) {
        return ApiResponse.ok(ticketService.accept(id, SecurityUtils.current()));
    }

    @PutMapping("/{id}/start")
    public ApiResponse<Ticket> start(@PathVariable Long id) {
        return ApiResponse.ok(ticketService.start(id, SecurityUtils.current()));
    }

    @PutMapping("/{id}/finish")
    public ApiResponse<Ticket> finish(@PathVariable Long id, @Valid @RequestBody FinishTicketRequest request) {
        return ApiResponse.ok(ticketService.finish(id, request, SecurityUtils.current()));
    }

    @PutMapping("/{id}/confirm")
    public ApiResponse<Ticket> confirm(@PathVariable Long id) {
        return ApiResponse.ok(ticketService.confirm(id, SecurityUtils.current()));
    }

    @PutMapping("/{id}/rework")
    public ApiResponse<Ticket> rework(@PathVariable Long id, @Valid @RequestBody ReasonRequest request) {
        return ApiResponse.ok(ticketService.rework(id, request.getReason(), SecurityUtils.current()));
    }

    @PutMapping("/{id}/return")
    public ApiResponse<Ticket> returnTicket(@PathVariable Long id, @Valid @RequestBody ReasonRequest request) {
        return ApiResponse.ok(ticketService.returnTicket(id, request.getReason(), SecurityUtils.current()));
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<Ticket> cancel(@PathVariable Long id, @Valid @RequestBody ReasonRequest request) {
        return ApiResponse.ok(ticketService.cancel(id, request.getReason(), SecurityUtils.current()));
    }

    @PostMapping("/{id}/remarks")
    public ApiResponse<Void> remark(@PathVariable Long id, @Valid @RequestBody RemarkRequest request) {
        ticketService.remark(id, request.getContent(), SecurityUtils.current());
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/urge")
    public ApiResponse<Void> urge(@PathVariable Long id) {
        ticketService.urge(id, SecurityUtils.current());
        return ApiResponse.ok();
    }
}

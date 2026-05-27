package expense_splitter.controller;

import expense_splitter.dto.GroupRequest;
import expense_splitter.dto.GroupResponse;
import expense_splitter.dto.MemberRequest;
import expense_splitter.service.GroupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/groups")
// all endpoints start with /api/groups
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping
    // POST /api/groups → create a group
    public ResponseEntity<GroupResponse> createGroup(
            @Valid @RequestBody GroupRequest request) {

        GroupResponse response = groupService.createGroup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
        // 201 CREATED — more accurate than 200 OK
        // when something new is created → always return 201
    }

    @GetMapping
    // GET /api/groups → get all my groups
    public ResponseEntity<List<GroupResponse>> getMyGroups() {

        return ResponseEntity.ok(groupService.getMyGroups());
        // 200 OK with list of groups
    }

    @GetMapping("/{id}")
    // GET /api/groups/{id} → get one group
    public ResponseEntity<GroupResponse> getGroupById(
            @PathVariable Long id) {
        // @PathVariable extracts {id} from URL
        // GET /api/groups/1 → id = 1

        return ResponseEntity.ok(groupService.getGroupById(id));
    }

    @PostMapping("/{id}/members")
    // POST /api/groups/1/members → add member to group
    public ResponseEntity<GroupResponse> addMember(
            @PathVariable Long id,
            @Valid @RequestBody MemberRequest request) {

        return ResponseEntity.ok(groupService.addMember(id, request));
    }

    @DeleteMapping("/{id}/members/{userId}")
    // DELETE /api/groups/1/members/2 → remove user 2 from group 1
    public ResponseEntity<GroupResponse> removeMember(
            @PathVariable Long id,
            @PathVariable Long userId) {
        // two @PathVariables — one for groupId, one for userId

        return ResponseEntity.ok(groupService.removeMember(id, userId));
    }
}
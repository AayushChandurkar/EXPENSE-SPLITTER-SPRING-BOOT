package expense_splitter.service;

import expense_splitter.dto.GroupRequest;
import expense_splitter.dto.GroupResponse;
import expense_splitter.dto.MemberRequest;
import expense_splitter.entity.Group;
import expense_splitter.entity.User;
import expense_splitter.exception.ResourceNotFoundException;
import expense_splitter.repository.GroupRepository;
import expense_splitter.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    public GroupService(GroupRepository groupRepository,
                        UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    // gets currently logged in user
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));
    }

    // converts Group entity to GroupResponse DTO
    private GroupResponse mapToResponse(Group group) {
        return GroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .createdBy(group.getCreatedBy().getName())
                .members(group.getMembers().stream()
                        .map(User::getName)
                        .collect(Collectors.toList()))
                .createdAt(group.getCreatedAt())
                .build();
    }

    @Transactional
    public GroupResponse createGroup(GroupRequest request) {

        User currentUser = getCurrentUser();
        // get who is making this request

        // build Group entity
        Group group = Group.builder()
                .name(request.getName())
                .description(request.getDescription())
                .createdBy(currentUser)
                .build();

        groupRepository.save(group);
        // save group first to get the id

        // add creator as first member
        // must update BOTH sides of ManyToMany
        group.getMembers().add(currentUser);
        // add user to group's members set

        currentUser.getGroups().add(group);
        // add group to user's groups set
        // User owns the relationship — must update user side
        // to persist to group_members table

        userRepository.save(currentUser);
        // saving user persists to group_members table

        return mapToResponse(group);
    }

    @Transactional
    public List<GroupResponse> getMyGroups() {

        User currentUser = getCurrentUser();

        return currentUser.getGroups().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public GroupResponse getGroupById(Long groupId) {

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Group not found with id: " + groupId));

        return mapToResponse(group);
    }

    @Transactional
    public GroupResponse addMember(Long groupId,
                                   MemberRequest request) {

        // find the group
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Group not found with id: " + groupId));

        // find the user to add
        User userToAdd = userRepository.findById(request.getUserId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: "
                                        + request.getUserId()));

        // check if user is already a member
        if (group.getMembers().contains(userToAdd)) {
            throw new RuntimeException("User is already a member");
        }

        // must update BOTH sides of ManyToMany relationship
        group.getMembers().add(userToAdd);
        // add user to group's members set

        userToAdd.getGroups().add(group);
        // add group to user's groups set
        // User owns the relationship — this persists to DB

        userRepository.save(userToAdd);
        // saving user persists group_members record

        return mapToResponse(group);
    }

    @Transactional
    public GroupResponse removeMember(Long groupId, Long userId) {

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Group not found with id: " + groupId));

        User userToRemove = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + userId));

        // check if user is actually a member
        if (!group.getMembers().contains(userToRemove)) {
            throw new RuntimeException(
                    "User is not a member of this group");
        }

        // must update BOTH sides of ManyToMany
        group.getMembers().remove(userToRemove);
        userToRemove.getGroups().remove(group);
        // remove from both sides

        userRepository.save(userToRemove);
        // persists the removal to group_members table

        return mapToResponse(group);
    }
}
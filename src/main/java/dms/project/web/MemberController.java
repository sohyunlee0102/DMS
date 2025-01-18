package dms.project.web;

import dms.project.domain.Member;
import dms.project.dto.MemberRequestDto;
import dms.project.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @PostMapping
    public ResponseEntity<Member> createMember(@RequestBody MemberRequestDto.JoinDto request) {
        Member member = memberService.createMember(request);
        return ResponseEntity.ok(member);
    }

}

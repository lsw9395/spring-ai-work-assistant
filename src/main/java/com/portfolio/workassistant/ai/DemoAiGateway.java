package com.portfolio.workassistant.ai;

import com.portfolio.workassistant.chat.ChatRequest;

public class DemoAiGateway implements AiGateway {
    @Override public String mode() { return "demo"; }

    @Override public String answer(ChatRequest request) {
        String input = request.message().trim();
        String preview = input.substring(0, Math.min(input.length(), 220));
        String body = switch (request.task()) {
            case MEETING -> "회의록 정리 양식\n\n1. 논의 내용\n" + preview
                    + "\n\n2. 결정 사항\n• 확정된 결정과 추가 검토 항목을 구분해 작성하세요."
                    + "\n\n3. 후속 업무\n• 담당자: [이름]\n• 기한: [날짜]\n• 할 일: [구체적인 행동]";
            case EMAIL -> "제목: [업무명] 확인 및 회신 요청\n\n안녕하세요, [담당자]님.\n\n"
                    + "다음 내용에 대해 확인을 요청드립니다.\n" + preview
                    + "\n\n검토 후 [회신 기한]까지 의견 부탁드립니다.\n추가로 필요한 자료가 있다면 말씀해 주세요.\n\n감사합니다.\n[이름] 드림";
            case CHECKLIST -> "업무 체크리스트 양식\n\n대상 업무: " + preview
                    + "\n\n□ 업무 목표와 완료 기준 확인\n□ 필요한 자료 및 담당자 확인"
                    + "\n□ 세부 일정과 선행 작업 확인\n□ 진행 결과 검토 및 누락 점검\n□ 결과 공유 및 후속 업무 등록";
            case GENERAL -> "요청을 확인했습니다.\n\n“" + preview
                    + "”\n\n업무를 다음 순서로 정리해 보세요.\n1. 목적과 원하는 결과 정의"
                    + "\n2. 현재 상황과 필요한 자료 정리\n3. 실행할 일과 담당자 지정\n4. 완료 기한과 확인 방법 설정";
        };
        return "[데모 응답 · AI 생성 아님]\n입력을 양식에 배치한 예시입니다. 내용 분석이나 사실 확인은 수행하지 않습니다.\n\n" + body;
    }
}

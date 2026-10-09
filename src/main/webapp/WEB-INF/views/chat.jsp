<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <meta name="description" content="회의록, 업무 메일, 체크리스트를 정리하는 AI 업무 도우미 포트폴리오">
  <title>AI Work Assistant · 업무의 다음 단계</title>
  <link rel="stylesheet" href="<c:url value='/css/app.css'/>">
  <link rel="stylesheet" href="<c:url value='/css/compact.css'/>">
  <script defer src="<c:url value='/vendor/jquery.min.js'/>"></script>
  <script defer src="<c:url value='/js/app.js'/>"></script>
</head>
<body data-api-base="<c:url value='/api'/>">
<aside class="sidebar">
  <a class="brand" href="<c:url value='/'/>"><span class="brand-mark">w<span>•</span></span><span>work assistant<small>YOUR DAILY WORKSPACE</small></span></a>
  <button id="new-chat" class="new-chat" type="button"><span>＋</span> 새 대화 시작</button>
  <div class="nav-label">WORKSPACE</div>
  <div class="nav-active"><span>◈</span> AI 업무 도우미 <span class="nav-dot"></span></div>
  <div class="sidebar-note"><span class="note-icon">↗</span><h2>작은 정리, 더 나은 업무.</h2><p>흩어진 생각은 명확하게,<br>반복되는 업무는 간결하게.</p></div>
  <div class="sidebar-bottom"><span class="avatar">P</span><div>Portfolio Project<small>Spring Boot · JSP · jQuery</small></div><span class="version">v1.0</span></div>
</aside>
<main>
  <header class="topbar"><div>Workspace <span>/</span> <strong>업무 도우미</strong></div><span id="mode-badge" class="badge">연결 확인 중</span></header>
  <div class="workspace">
    <section id="welcome" class="welcome">
      <div class="eyebrow"><span></span> MAKE ROOM FOR WHAT MATTERS</div>
      <h1>오늘의 업무,<br><span>조금 더 가볍게.</span></h1>
      <p class="intro">정리가 필요한 순간, 함께 시작해 보세요.<br>회의 메모부터 업무 메일까지, 다음 단계를 도와드릴게요.</p>
      <div class="cards">
        <button type="button" class="task-card" data-task="MEETING"><span class="card-icon mint">▤</span><strong>회의록 정리 <span>↗</span></strong><p>긴 메모를 핵심과 할 일로</p><small>요약 · 결정 사항 · 후속 업무</small></button>
        <button type="button" class="task-card" data-task="EMAIL"><span class="card-icon blue">✉</span><strong>업무 메일 작성 <span>↗</span></strong><p>전하고 싶은 말을 명확하게</p><small>요청 · 회신 · 안내 메일</small></button>
        <button type="button" class="task-card" data-task="CHECKLIST"><span class="card-icon sand">☷</span><strong>체크리스트 만들기 <span>↗</span></strong><p>빠뜨림 없이 한 단계씩</p><small>업무 계획 · 점검 · 인수인계</small></button>
      </div>
      <div class="example"><span>TRY THIS</span><button id="example" type="button">“신규 시스템 도입 회의 내용을 정리해 줘” <span>↗</span></button></div>
    </section>
    <section id="messages" class="messages" role="log" aria-label="대화 내용" aria-live="polite" aria-relevant="additions"></section>
    <div id="loading" class="loading" role="status" hidden><span class="pulse"></span> 응답을 준비하고 있어요…</div>
    <div id="error" class="error" role="alert" hidden><span id="error-text"></span><button id="retry" type="button">다시 시도</button></div>
    <div class="composer-wrap">
      <div id="mode-note" class="mode-note">실행 모드를 확인하고 있습니다.</div>
      <form id="chat-form" class="composer">
        <div class="composer-toolbar"><label for="task">작업 유형</label><select id="task" name="task"><option value="GENERAL">자유롭게 질문</option><option value="MEETING">회의록 정리</option><option value="EMAIL">업무 메일 작성</option><option value="CHECKLIST">체크리스트</option></select><span id="count">0 / 2,000</span></div>
        <label class="sr-only" for="message">업무 요청 입력</label>
        <textarea id="message" name="message" rows="3" maxlength="2000" placeholder="어떤 업무를 도와드릴까요? 내용을 입력해 주세요." required></textarea>
        <div class="composer-bottom"><span>Enter로 전송 · Shift + Enter로 줄바꿈</span><button id="send" type="submit" disabled>보내기 <span>↑</span></button></div>
      </form>
      <p class="footer-note">개인정보와 회사 기밀은 입력하지 마세요. 생성된 내용은 사용 전 검토해 주세요.</p>
    </div>
  </div>
</main>
</body>
</html>

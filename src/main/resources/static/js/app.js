/* global $ */
'use strict';
$(function () {
  const base = document.body.dataset.apiBase;
  let history = [], conversationId = crypto.randomUUID(), pending = false, ready = false, retryRequest = null;
  const samples = {
    MEETING: '다음 회의 메모를 핵심 논의, 결정 사항, 후속 업무로 정리해 줘.\n\n[가상 예시] 신규 재고관리 시스템 도입 회의\n- 현장 입력 누락을 줄이기 위해 모바일 입력 기능 검토\n- 시범 적용 부서는 물류팀으로 결정\n- 김담당: 다음 주 금요일까지 요구사항 정리\n- 이담당: 기존 데이터 이관 범위 확인',
    EMAIL: '협력사에 납품 일정 확인을 요청하는 정중한 메일을 써 줘.\n\n[가상 예시] 품목: 포장 자재 / 희망 납품일: 다음 주 수요일 / 확인할 내용: 수량 500개 공급 가능 여부와 예상 도착 시간',
    CHECKLIST: '신규 직원의 업무 시스템 계정 발급을 위한 체크리스트를 만들어 줘.\n인사정보 확인, 부서별 권한 승인, 계정 생성, 접속 테스트, 사용 안내를 포함해 줘.'
  };
  function sync() {
    $('#count').text($('#message').val().length.toLocaleString() + ' / 2,000');
    $('#send').prop('disabled', pending || !ready || !$('#message').val().trim());
    $('#message, #task, #new-chat, .task-card, #example, #retry').prop('disabled', pending);
    $('#loading').prop('hidden', !pending);
  }
  function addMessage(role, text, mode) {
    $('#welcome').prop('hidden', true);
    const row = $('<article>').addClass('message ' + role);
    $('<span>').addClass('message-icon').text(role === 'user' ? '나' : 'w').appendTo(row);
    const body = $('<div>').addClass('message-body').appendTo(row);
    $('<div>').addClass('message-label').text(role === 'user' ? '나의 요청' : 'Work Assistant · ' + (mode === 'demo' ? '데모' : 'AI')).appendTo(body);
    // Text nodes only: model output and user input must never become HTML.
    $('<div>').addClass('message-text').text(text).appendTo(body);
    if (role === 'assistant') $('<button>').attr('type', 'button').addClass('copy').text('응답 복사').on('click', async function () {
      try { await navigator.clipboard.writeText(text); $(this).text('복사 완료'); }
      catch (_) { $(this).text('텍스트를 선택해 복사해 주세요'); }
    }).appendTo(body);
    row.appendTo('#messages');
    row[0].scrollIntoView({ behavior: 'smooth', block: 'nearest' });
  }
  function send(request, isRetry) {
    if (pending) return;
    pending = true; retryRequest = request; $('#error').prop('hidden', true);
    if (!isRetry) addMessage('user', request.message);
    sync();
    $.ajax({ url: base + '/chat', method: 'POST', contentType: 'application/json',
      data: JSON.stringify(request), dataType: 'json', timeout: 35000
    }).done(function (response) {
      history.push({ role: 'user', content: request.message }, { role: 'assistant', content: response.answer });
      history = history.slice(-10);
      addMessage('assistant', response.answer, response.mode);
      $('#message').val(''); retryRequest = null;
    }).fail(function (xhr, status) {
      $('#error-text').text(status === 'timeout' ? '응답 대기 시간이 초과되었습니다. 다시 시도할 수 있습니다.' :
        (xhr.responseJSON && xhr.responseJSON.message) || '서버에 연결할 수 없습니다. 서버 실행 상태를 확인해 주세요.');
      $('#error').prop('hidden', false);
    }).always(function () { pending = false; sync(); $('#message').trigger('focus'); });
  }
  $('#chat-form').on('submit', function (event) {
    event.preventDefault();
    const message = $('#message').val().trim();
    if (!message || message.length > 2000 || pending || !ready) return;
    send({ conversationId, message, task: $('#task').val(), history: history.slice() }, false);
  });
  $('#message').on('input', sync).on('keydown', function (event) {
    if (event.key === 'Enter' && !event.shiftKey && !event.originalEvent.isComposing) {
      event.preventDefault(); $('#chat-form').trigger('submit');
    }
  });
  $('.task-card').on('click', function () {
    const task = this.dataset.task; $('#task').val(task); $('#message').val(samples[task]).trigger('focus'); sync();
  });
  $('#example').on('click', function () { $('[data-task="MEETING"]').trigger('click'); });
  $('#retry').on('click', function () { if (retryRequest) send(retryRequest, true); });
  $('#new-chat').on('click', function () {
    history = []; conversationId = crypto.randomUUID(); retryRequest = null;
    $('#messages').empty(); $('#error').prop('hidden', true); $('#welcome').prop('hidden', false);
    $('#message').val(''); $('#task').val('GENERAL'); sync(); $('#message').trigger('focus');
  });
  $.ajax({url: base + '/status', dataType: 'json', timeout: 5000}).done(function (status) {
    ready = true;
    const demo = status.mode === 'demo';
    $('#mode-badge').text(demo ? '● 데모 모드' : '● AI 연결 모드').toggleClass('live', !demo);
    $('#mode-note').text(demo ? '데모 모드 · API 키 없이 예시 응답을 체험할 수 있어요. 실제 AI 분석은 수행하지 않습니다.' :
      'AI 모드 · 입력과 최근 대화 최대 10개가 OpenAI로 전송됩니다. 대화는 새로고침하면 사라집니다.');
    sync();
  }).fail(function () {
    $('#mode-badge').text('연결 실패'); $('#mode-note').text('서버 연결을 확인한 뒤 페이지를 새로고침해 주세요.');
  });
  sync();
});

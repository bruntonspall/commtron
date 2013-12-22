$ ->
  $('.vote-up').click ->
    $.post($(this).data('target'))
    count = $(this).parent().find('.votecount')
    count.text(parseInt(count.text(),10)+1)
  $('.vote-down').click ->
    $.post($(this).data('target'))
    count = $(this).parent().find('.votecount')
    count.text(parseInt(count.text(),10)-1)
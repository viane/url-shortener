$(() => {

  const getRecord = () => {
    // clean Table
    $('#record-table-body').children().children().remove()
    const getUrl = "/test1/v1"
    const option = {
      method: 'get'
    }
    fetch(getUrl, option).then(res => {
      return res.text()
    }).then(resp => {
      const data = JSON.parse(resp);
      for (var i = 0; i < data.length; i++) {
        const jsonObj = data[i];
        // display table
        let jsonToTableEntry = "<tr>";
        jsonToTableEntry += "<td>" + decodeURIComponent(jsonObj.o_url) + "</td>";

        jsonToTableEntry += "<td><a target='_blank' href='" + window.location.href + "r/" + jsonObj.h_url + "'>" + jsonObj.h_url + "</a></td>";
        if (jsonObj.is_private=="0") {
          jsonToTableEntry += "<td>False</td>";
        } else {
          jsonToTableEntry += "<td>True</td>";
        }
        const expireAfter = moment().to(moment(jsonObj.expire_time));
        jsonToTableEntry += "<td>" + expireAfter + "</td>";
        jsonToTableEntry += "</tr>";

        $('#record-table-body > tbody:last-child').append(jsonToTableEntry);
      }
      console.log(data)
    }).catch(function(err) {
      console.error(err)
    })
  }

  getRecord();

  $(window).on("load resize ", function() {
    var scrollWidth = $('.tbl-content').width() - $('.tbl-content table').width();
    $('.tbl-header').css({'padding-right': scrollWidth});
  }).resize();

  const urlValidator = (url) => {
    return /^(https?|s?ftp):\/\/(((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:)*@)?(((\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5]))|((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.?)(:\d*)?)(\/((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)+(\/(([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)*)*)?)?(\?((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)|[\uE000-\uF8FF]|\/|\?)*)?(#((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)|\/|\?)*)?$/i.test(url);
  }

  const submitShortenReuqest = () => {

    // disable button
    $('#submit').attr("disabled", "disabled")
    const o_url = $('#url-field').val().trim()

    if (!urlValidator(o_url)) {
      $.notify({
        title: 'Error',
        message: 'Invalid Url'
      }, {
        type: 'warning',
        animate: {
          enter: 'animated fadeInDown',
          exit: 'animated fadeOutUp'
        },
        allow_dismiss: true
      })
    } else {
      $.notify({
        title: 'Please wait',
        message: 'Requesting to server'
      }, {
        type: 'info',
        animate: {
          enter: 'animated fadeInDown',
          exit: 'animated fadeOutUp'
        },
        allow_dismiss: true
      })
      let is_private = 0
      if ($('#access-type')[0].checked) {
        is_private = 1
      }
      const expire_time = $('#expire-time').val() || 1
      const owner = $('#userid').text().trim();
      const level = $('#hash-level').val() || 1

      const fetch_body = encodeURIComponent(o_url) + "&" + is_private + "&" + expire_time + "&" + owner + "&" + level

      const postUrl = "/test1/v1"
      const option = {
        method: 'post',
        'Content-Type': 'text/plain; charset=utf-8',
        body: fetch_body
      }

      fetch(postUrl, option).then(res => {
        const resp = res.text();
        console.log(resp)
        // enable button
        $('#submit').removeAttr('disabled')
        return resp
      }).then(data => {
        // refresh table
        getRecord()
        const resObj = JSON.parse(data)
        $.notify({
          title: 'Response',
          message: resObj.message
        }, {
          type: 'info',
          animate: {
            enter: 'animated fadeInDown',
            exit: 'animated fadeOutUp'
          },
          allow_dismiss: true
        })
        console.log(resObj);
      }).catch(function(err) {
        console.error(err)
      })
    }
    $('#submit').removeAttr('disabled')

  }

  $('#submit').click(() => {
    submitShortenReuqest()
  })

  $("#url-field").keypress(function(event) {
    if (event.which == 13) {
      event.preventDefault()
      $('#submit').click()
    }
  })
})

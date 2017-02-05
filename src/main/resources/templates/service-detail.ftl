<#import "spring.ftl" as spring/>
<#import 'layout/default.ftl' as layout>

<@layout.default>

<div class="page-header">
  <h1>Notify service</h1>
</div>
<h2>Service Details</h2><!-- TODO: -->
<p>${se.service?html}</p>
<p>${se.type?html}</p>
<p>${se.description?html}</p>

<h2>Available access token</h2>
<a href="#fmNewAccessToken" class="btn btn-sm btn-primary" data-toggle="modal">New Private Access Token</a>
<div class="table-responsive">

  <table class="table table-striped table-hover">
    <caption><span class="glyphicon glyphicon-user" aria-hidden="true"></span>&nbsp;LINE Notify Access Token
    </caption>
    <thead>
    <tr>
      <th>Private Access Token</th>
      <th>Owner</th>
      <th>Description</th>
      <th>&nbsp;</th>
    </tr>
    </thead>
    <tbody>
<#list to as t>
    <tr id="tr-${t.id?html}">
      <td>${t.token?html}</td>
      <td>${t.owner?html}</td>
      <td>${t.description?html}</td>
      <td><button type="button" class="close" aria-label="Close" onclick="invalidate('${t.id?html}')"><span aria-hidden="true">&times;</span></button></td>
    </tr>
</#list>
    </tbody>
  </table>
</div>

<h2>Message Templates</h2>

<div class="row">
  <div class="col-md-4">.col-md-4
  </div>
  <div class="col-md-4">.col-md-4
  </div>
  <div class="col-md-4">.col-md-4
  </div>
</div>

<!-- dialogs -->
<div id="fmNewAccessToken" class="modal fade bs-example-modal-lg" tabindex="-1" role="dialog">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <h4 class="modal-title">New LINE Private Access Token</h4>
      </div>
      <div class="modal-body">
        <div>
          <form class="form-horizontal">
            <div class="form-group">
              <label for="accessToken">Your Private Access Token</label>
              <input type="text" class="form-control" id="accessToken"
                     placeholder="Your Private Access Token issue from https://notify-bot.line.me">
            </div>
            <div class="form-group">
              <label for="owner">Owner</label>
              <input type="text" class="form-control" id="accessToken" placeholder="Your LINE ID @xxxx">
            </div>
            <div class="form-group">
              <label for="description">Description</label>
              <input type="text" class="form-control" id="accessToken"
                     placeholder="Description. ex: Messaging LINE Group">
            </div>
          </form>
        </div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
        <button type="button" class="btn btn-primary">Save changes</button>
      </div>
    </div>
  </div>
</div>

<script>
  function invalidate(tokenId) {
    $.ajax({
             type: 'DELETE',
             url: '/api/token?id=' + encodeURIComponent(tokenId),
             dataType: 'json'
           }).done(function (data) {
      $('#tr-' + tokenId).remove();
    }).fail(function (data) {
    })
  }
</script>


</@layout.default>

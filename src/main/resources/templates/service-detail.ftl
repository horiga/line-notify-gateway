<#import "spring.ftl" as spring/>
<#import 'layout/default.ftl' as layout>

<@layout.default>

<h2>service.name</h2>

<h3>LINE Notify accessToken</h3>

<table class="table table-striped table-bordered">
  <thead>
  <tr>
    <th>LINE Notify Private Access Token</th>
    <th>Description</th>
    <th>Owner</th>
    <th>&nbsp;</th>
  </tr>
  </thead><tbody>
  <#list tokens as token>
  <tr id="tr-${token.id?html}">
    <td>${token.token?html}</td><td>${token.description?html}</td><td>${token.owner?html}</td>
    <td><button class="btn btn-default" aria-label="Left Align">
      <span class="glyphicon glyphicon-remove" aria-hidden="true" onclick="invalidate('${token.id?html}')"></span>
    </button></td>
  </tr>
  </#list></tbody>
</table>

<script>
  function invalidate(tokenId) {
    $.ajax({
      type: 'DELETE',
      url: '/api/token?id=' + encodeURIComponent(tokenId),
      dataType: 'json'
    }).done(function (data) {
      $('#tr-'+tokenId).remove();
    }).fail(function (data) {
    })
  }
</script>
</@layout.default>

<#import "spring.ftl" as spring/>
<#import 'layout/default.ftl' as layout>

<@layout.default>

<div class="list-group">
  <#list services as service>
  <a href="/admin/service/${service.service?html}" class="list-group-item">
    <h4 class="list-group-item-heading">${service.service?html}</h4>
    <p class="list-group-item-text">${service.description?html}</p>
  </a>
  </#list>
</div>

<script>
</script>
</@layout.default>

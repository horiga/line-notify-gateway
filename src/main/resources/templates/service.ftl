<#import "spring.ftl" as spring/>
<#import 'layout/default.ftl' as layout>
<@layout.default>
<div class="section">
  <h2 class="header">Services</h2>
    <p class="flow-text">xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx</p>
</div>
<table class="responsive-table">
  <thead>
    <tr>
      <th data-field="displayName">Service</th>
      <th data-field="type">Type</th>
      <th data-field="description">Description</th>
      <th data-field="templateGroupMappingType">Templates Mapping type</th>
      <th data-field="templateGroupMappingValue">Templates Mapping values</th>
      <th></th>
    </tr>
  </thead>
  <tbody>
  <#list services as s>
    <tr>
      <td><a href="/console/service/${s.serviceId?html}">${s.displayName?html}</a></td>
      <td>${s.type?html}</td>
      <td>${s.description?html}</td>
      <td>${s.templateMappingType?html}</td>
      <td>${s.templateMappingValue?html}</td>
      <td><a href="/console/template/${s.templateGroupId?html}" class="waves-effect waves-light btn">Templates</a></td>
    </tr>
  </#list>
  </tbody>
</table>
<script>
</script>
</@layout.default>

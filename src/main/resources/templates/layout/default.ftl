<#macro default>
<!DOCTYPE><html>
<head>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<!--/css-->
<!--/js-->
</head><body><div class="container">
<#if breadcrumbs?has_content>
<ol class="breadcrumb">
<#list breadcrumbs.contents as value>
<#if value_has_next><li><a href="${value.link}">${value.text}</a></li>
<#else>
<li class="active">${value.text}</li>
</#if>
</#list>
</ol>
</#if>
<div class="main-page">
<#nested />
</div><!-- main-page -->
</div><!-- container --></body></html>
</#macro>
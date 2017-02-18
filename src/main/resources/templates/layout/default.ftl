<#macro default>
<!DOCTYPE><html>
<head>
  <meta charset="utf-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Notify Center</title>
  <!--Import Google Icon Font-->
  <link href="http://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
  <!--Import materialize.css-->
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/materialize/0.98.0/css/materialize.min.css">
  <link rel="stylesheet" href="/css/parsley.css">
  <link rel="stylesheet" href="/css/style.css">
  <!--Import jQuery before materialize.js-->
  <script type="text/javascript" src="https://code.jquery.com/jquery-2.1.1.min.js"></script>
  <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/materialize/0.98.0/js/materialize.min.js"></script>
  <script type="text/javascript" src="/js/parsley.min.js"></script>
  <!--Let browser know website is optimized for mobile-->
  <title>Notify Gateway</title>
</head>
<body>
<header>
<!-- Navigation bar -->
<nav class="darken-3">
  <div class="container nav-header nav-fixed lighten-5">
    <a href="/" class="brand-logo">Notify</a>
    <ul id="nav-mobile" class="right hide-on-med-and-down">
      <li><a href="/console/service">Service</a></li>
      <li><a href="/console/template">Template</a></li>
      <li><a href="http://" target="_blank">History</a></li>
    </ul>
  </div>
</nav>
</header><!-- // nav-container -->
<main>
  <div class="container">
<!-- //main-page -->
<#nested />
  </div>
</main>

<!-- Footer -->
<#--<footer class="page-footer">-->
  <#--<div class="footer-copyright">-->
    <#--<div class="container">-->
      <#--© 2017 Copyright h.horiga-->
      <#--<a class="grey-text text-lighten-4 right" href="https://github.com/horiga/line-notify-gateway">github</a>-->
    <#--</div>-->
  <#--</div>-->
<#--</footer>-->
<!-- //page-footer -->

</body></html>
</#macro>
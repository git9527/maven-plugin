maven-plugin项目

**推荐使用项目中的settings.xml作为maven的配置文件**

使用方法:
1.在pom.xml中的如下位置添加插件
&lt;build&gt;<br>&nbsp; ...<br>&nbsp; &lt;plugins&gt;<br>&nbsp;&nbsp;&nbsp; ...<br>&nbsp;&nbsp;&nbsp; &lt;plugin&gt;<br>&nbsp;&nbsp; &nbsp;&lt;groupId&gt;com.travelsky&lt;/groupId&gt;<br>&nbsp;&nbsp; &nbsp;&lt;artifactId&gt;deploy-plugin&lt;/artifactId&gt;<br>&nbsp;&nbsp; &nbsp;&lt;version&gt;1.0&lt;/version&gt;<br>&nbsp;&nbsp; &nbsp;&lt;configuration&gt;<br>&nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &nbsp;&lt;artifactId&gt;demo-api&lt;/artifactId&gt;<br>&nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &nbsp;&lt;version&gt;2.0&lt;/version&gt;<br>&nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &nbsp;&lt;includes&gt;<br>&nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &nbsp;&lt;include&gt;com/travelsky/api/**&lt;/include&gt;<br>&nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &nbsp;&lt;include&gt;com/travelsky/dto/**&lt;/include&gt;<br>&nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &nbsp;&lt;/includes&gt;<br>&nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &nbsp;&lt;/configuration&gt;<br>&nbsp;&nbsp;&nbsp; &lt;/plugin&gt;<br>&nbsp; &lt;/plugins&gt;<br>&lt;/build&gt;
2.调用maven指令travelsky:deploy即可


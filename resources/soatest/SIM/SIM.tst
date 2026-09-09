---
parasoftVersion: 2026.1.0
productVersion: 10.7.5
schemaVersion: 03
suite:
  $type: TestSuite
  name: Test Suite
  hasEnvironmentConfig: true
  lastModifiedBy: bmcmullin
  performance:
    groups:
    - name: /admin/db/backup - GET
    - id: 1
      name: /admin/db/import - POST
    - id: 2
      name: /admin/email/config - GET
    - id: 3
      name: /admin/email/config - POST
    - id: 3
      name: /admin/email/send - POST
    - id: 4
      name: /admin/salesforce/oauth/callback - GET
    - id: 5
      name: /admin/salesforce/oauth/start - GET
    - id: 6
      name: /admin/save-config - POST
    - id: 7
      name: /admin/sync-customer-profile - POST
    - id: 8
      name: /admin/terms - GET
    - id: 9
      name: /admin/terms - PUT
    - id: 10
      name: /admin/terms - POST
    - id: 11
      name: /admin/terms - DELETE
    - id: 9
      name: /admin/terms/export - GET
    - id: 10
      name: /admin/terms/import - POST
    - id: 11
      name: /admin/test-connection - POST
    - id: 12
      name: /admin/test-salesforce-connection - POST
    - id: 13
      name: /admin/users - GET
    - id: 14
      name: /admin/users - POST
    - id: 15
      name: /admin/users - DELETE
    - id: 14
      name: /admin/widget-health-config - GET
    - id: 15
      name: /admin/widget-health-config - POST
    - id: 15
      name: /admin/widgets - GET
    - id: 16
      name: /admin/widgets - POST
    - id: 17
      name: /admin/widgets - DELETE
    - id: 16
      name: /admin/widgets/sync - GET
    - id: 17
      name: /admin/widgets/sync - POST
    - id: 17
      name: /admin/widgets/sync/timer - GET
    - id: 18
      name: /admin/widgets/sync/timer - POST
    - id: 18
      name: /admin/workspace - POST
    - id: 19
      name: /api/auth/login - POST
    - id: 20
      name: /dashboard/drilldown/widget-review/job-status - GET
    - id: 21
      name: /dashboard/drilldown/widget-review/job-status - DELETE
    - id: 21
      name: /dashboard/sessions/chats - GET
    - id: 22
      name: /dashboard/sessions/chats - POST
    - id: 22
      name: /dashboard/sessions/data - GET
    - id: 23
      name: /dashboard/sessions/data - POST
    - id: 23
      name: /dashboard/sessions/select - GET
    - id: 24
      name: /dashboard/sessions/select - POST
    - id: 24
      name: /dashboard/topics/data - GET
    - id: 25
      name: /dashboard/topics/select - POST
    - id: 26
      name: /dashboard/trends/select - POST
    - id: 27
      name: /dashboard/widgets - GET
    - id: 28
      name: /dashboard/widgets - POST
    - id: 29
      name: /dashboard/widgets - DELETE
    - id: 28
      name: /dashboard/widgets/drilldown/review/translate - GET
    - id: 29
      name: /dashboard/widgets/drilldown/review/translate - POST
    - id: 29
      name: /dashboard/widgets/drilldown/view/data - GET
    - id: 30
      name: /dashboard/widgets/drilldown/view/review-data - GET
    - id: 31
      name: /dashboard/widgets/review/start - POST
  notes: |-
    Created from: file:///D:/Code_Workspace/project/SIM/docs/swagger/sim-api-soatest-openapi.yaml
    Timestamp: 2026-07-27 13:57:06
  tests:
  - $type: TestSuite
    name: Auth Login
    testID: 84
    tests:
    - $type: RESTClientToolTest
      name: /api/auth/login - POST
      testID: 85
      performanceGroup: 19
      tool:
        $type: RESTClient
        iconName: RESTClient
        name: /api/auth/login - POST
        formJson:
          builtFromSchema: true
          value:
            $type: ElementValue
            writeType: true
            type:
              $type: ElementType
              localName: root
              bodyType:
                $type: ComplexType
                attributes:
                - ns: ""
                  name: type
                  defaultValue: object
                  fixed: object
                  contentType:
                    $type: StringType
                    _id: 1
                  required: true
                name: /components/schemas/LoginRequest
                compositor: true
                compositorObj:
                  $type: AllCompositor
                  types:
                  - $type: ElementType
                    localName: username
                    bodyType:
                      $type: ComplexType
                      _id: 2
                      attributes:
                      - ns: ""
                        name: type
                        defaultValue: string
                        fixed: string
                        contentType:
                          $type: StringType
                          _ref: 1
                        required: true
                      content: true
                      contentObj:
                        $type: StringType
                        _ref: 1
                      namespace: urn:parasoft:json
                      name: string
                      compositor: true
                      compositorObj:
                        $type: SequenceCompositor
                  - $type: ElementType
                    localName: password
                    bodyType:
                      $type: ComplexType
                      _ref: 2
            replacedColumn: ""
            values:
            - $type: ComplexValue
              replacedColumn: ""
              attributes:
              - replacedColumn: ""
                value:
                  $type: StringValue
                  replacedColumn: ""
                  value: object
                useValue: true
              compositorValue: true
              compositorValueObj:
                replacedColumn: ""
                values:
                  $type: CompositorValueSetCollectionSet
                  set:
                  - $type: CompositorValueSet
                    values:
                    - $type: ElementValue
                      replacedColumn: ""
                      values:
                      - $type: ComplexValue
                        replacedColumn: ""
                        attributes:
                        - replacedColumn: ""
                          value:
                            $type: StringValue
                            replacedColumn: ""
                            value: string
                          useValue: true
                        contentValue: true
                        contentValueObj:
                          $type: StringValue
                          replacedColumn: ""
                          value: admin
                        compositorValue: true
                        compositorValueObj:
                          replacedColumn: ""
                          values:
                            $type: CompositorValueSetCollectionSet
                            set:
                            - $type: CompositorValueSet
                    - $type: ElementValue
                      replacedColumn: ""
                      values:
                      - $type: ComplexValue
                        replacedColumn: ""
                        attributes:
                        - replacedColumn: ""
                          value:
                            $type: StringValue
                            replacedColumn: ""
                            value: string
                          useValue: true
                        contentValue: true
                        contentValueObj:
                          $type: StringValue
                          replacedColumn: ""
                          value: admin
                        compositorValue: true
                        compositorValueObj:
                          replacedColumn: ""
                          values:
                            $type: CompositorValueSetCollectionSet
                            set:
                            - $type: CompositorValueSet
          elementTypeName: root
        hasServiceInfo: true
        serviceInfo:
          serviceDescriptor:
            $type: StandardServiceDescriptor
            location: file:///D:/Code_Workspace/project/SIM/docs/swagger/sim-api-soatest-openapi.yaml
          serviceName: ""
          versionName: ""
        jsonBuilder:
          hasValue: true
          value:
            $type: JSONObjectValue
            nameIsNull: true
        schemaURL:
          MessagingClient_SchemaLocation: file:///D:/Code_Workspace/project/SIM/docs/swagger/sim-api-soatest-openapi.yaml
        formInput:
          value:
            $type: ElementValue
            writeType: true
            type:
              $type: ElementType
              localName: ""
              bodyType:
                $type: ComplexType
                name: anonymous
                compositor: true
                compositorObj:
                  $type: SequenceCompositor
            replacedColumn: ""
            values:
            - $type: ComplexValue
              replacedColumn: ""
              compositorValue: true
              compositorValueObj:
                replacedColumn: ""
                values:
                  $type: CompositorValueSetCollectionSet
                  set:
                  - $type: CompositorValueSet
        jmsMessageOutputProvider:
          $type: JMSMessageOutputProvider
          jmsOutputProviderRequest:
            $type: JMSOutputProvider
            name: Request Object
            menuName: Object
          jmsOutputProviderResponse:
            $type: JMSOutputProvider
            name: Response Message Object
            menuName: Message Object
        validResponseRange:
          validResponseRange:
            values:
            - $type: ScriptedValue
            fixedValue:
              $type: StringTestValue
              validResponseRange: 200
        router:
          values:
          - $type: ScriptedValue
          fixedValue:
            $type: StringTestValue
            HTTPClient_Endpoint: http://localhost/chat-server/api/auth/login
        transportProperties:
          manager:
            protocol: 1
            properties:
            - $type: HTTPClientHTTPProperties
              followRedirects:
                bool: true
              common:
                method:
                  values:
                  - $type: ScriptedValue
                  fixedValue:
                    $type: HTTPMethodTestValue
                    method: POST
                httpHeaders:
                  properties:
                  - name: Accept
                    value:
                      values:
                      - $type: ScriptedValue
                      fixedValue:
                        $type: StringTestValue
                        value: application/json
              protocol: 1
              keepAlive1_1:
                bool: true
          messageExchangePattern:
            inverted: true
        outputProviders:
          requestHeader:
            $type: HTTPNamedToolOutputProvider
            menuName: Transport Header
            name: Request Transport Header
          responseHeader:
            $type: HTTPNamedToolOutputProvider
            menuName: Transport Header
            name: Response Transport Header
          xmlRequestOutput:
            $type: NamedXMLToolOutputProvider
            menuName: Traffic
            name: Request Traffic
          trafficOutput:
            m_name: Traffic Stream
          objectOutput:
            $type: ObjectOutputProvider
            outputTools:
            - $type: TrafficViewer
              iconName: TrafficViewer
              name: Traffic Viewer
              showRequestHeaders: true
            name: Traffic Object
        formXML:
          doctype: ""
        literal:
          use: 1
          text:
            MessagingClient_LiteralMessage: "{\"username\":\"admin\",\"password\"\
              :\"change-me\"}"
            type: application/json
        mode: Form JSON
        literalQuery:
          isPropertiesRef: true
        literalPath:
          pathElements:
          - values:
            - $type: ScriptedValue
            fixedValue:
              $type: StringTestValue
              value: chat-server
          - values:
            - $type: ScriptedValue
            fixedValue:
              $type: StringTestValue
              value: api
          - values:
            - $type: ScriptedValue
            fixedValue:
              $type: StringTestValue
              value: auth
          - values:
            - $type: ScriptedValue
            fixedValue:
              $type: StringTestValue
              value: login
        resourceMethod:
          resourceId: /api/auth/login
          httpMethod: POST
        resourceMode: 3
        baseUrl:
          values:
          - $type: ScriptedValue
          - $type: WadlTestValue
            value: http://localhost/chat-server
          fixedValue:
            $type: StringTestValue
            value: http://localhost/chat-server
          selectedIndex: 1
  - $type: TestSuite
    name: SIM endpoints
    testID: 86
    tests:
    - $type: TestSuite
      name: /admin/db/backup
      testID: 87
      tests:
      - $type: RESTClientToolTest
        name: /admin/db/backup - GET
        testID: 88
        performanceGroup: 0
        tool:
          $type: RESTClient
          iconName: RESTClient
          name: /admin/db/backup - GET
          formJson:
            value:
              $type: ElementValue
              writeType: true
              type:
                $type: ElementType
                localName: root
                bodyType:
                  $type: ComplexType
                  attributes:
                  - ns: ""
                    name: type
                    fixed: object
                    contentType:
                      $type: StringType
                    required: true
                  name: rootType
                  compositor: true
                  compositorObj:
                    $type: AllCompositor
              replacedColumn: ""
              values:
              - $type: ComplexValue
                replacedColumn: ""
                attributes:
                - replacedColumn: ""
                  value:
                    $type: StringValue
                    replacedColumn: ""
                    value: object
                  useValue: true
                compositorValue: true
                compositorValueObj:
                  replacedColumn: ""
                  values:
                    $type: CompositorValueSetCollectionSet
                    set:
                    - $type: CompositorValueSet
            elementTypeName: root
          hasServiceInfo: true
          serviceInfo:
            serviceDescriptor:
              $type: StandardServiceDescriptor
              location: file:///D:/Code_Workspace/project/SIM/docs/swagger/sim-api-soatest-openapi.yaml
            serviceName: ""
            versionName: ""
          jsonBuilder:
            hasValue: true
            value:
              $type: JSONObjectValue
              nameIsNull: true
          schemaURL:
            MessagingClient_SchemaLocation: file:///D:/Code_Workspace/project/SIM/docs/swagger/sim-api-soatest-openapi.yaml
          formInput:
            value:
              $type: ElementValue
              writeType: true
              hasReference: true
              qnameAsString: ":"
              replacedColumn: ""
              values:
              - $type: ComplexValue
                replacedColumn: ""
                compositorValue: true
                compositorValueObj:
                  replacedColumn: ""
                  values:
                    $type: CompositorValueSetCollectionSet
                    set:
                    - $type: CompositorValueSet
          jmsMessageOutputProvider:
            $type: JMSMessageOutputProvider
            jmsOutputProviderRequest:
              $type: JMSOutputProvider
              name: Request Object
              menuName: Object
            jmsOutputProviderResponse:
              $type: JMSOutputProvider
              name: Response Message Object
              menuName: Message Object
          validResponseRange:
            validResponseRange:
              values:
              - $type: ScriptedValue
              fixedValue:
                $type: StringTestValue
                validResponseRange: 200
          router:
            values:
            - $type: ScriptedValue
            fixedValue:
              $type: StringTestValue
              HTTPClient_Endpoint: http://localhost/chat-server/admin/db/backup
          transportProperties:
            manager:
              protocol: 1
              properties:
              - $type: HTTPClientHTTPProperties
                followRedirects:
                  bool: true
                common:
                  method:
                    values:
                    - $type: ScriptedValue
                    fixedValue:
                      $type: HTTPMethodTestValue
                      method: GET
                  httpHeaders:
                    properties:
                    - name: Accept
                      value:
                        values:
                        - $type: ScriptedValue
                        fixedValue:
                          $type: StringTestValue
                          value: application/json
                protocol: 1
                keepAlive1_1:
                  bool: true
            messageExchangePattern:
              inverted: true
          outputProviders:
            requestHeader:
              $type: HTTPNamedToolOutputProvider
              menuName: Transport Header
              name: Request Transport Header
            responseHeader:
              $type: HTTPNamedToolOutputProvider
              menuName: Transport Header
              name: Response Transport Header
            xmlRequestOutput:
              $type: NamedXMLToolOutputProvider
              menuName: Traffic
              name: Request Traffic
            trafficOutput:
              m_name: Traffic Stream
            objectOutput:
              $type: ObjectOutputProvider
              outputTools:
              - $type: TrafficViewer
                iconName: TrafficViewer
                name: Traffic Viewer
                showRequestHeaders: true
              name: Traffic Object
          formXML:
            doctype: ""
          literal:
            use: 1
            text:
              MessagingClient_LiteralMessage: ""
              type: application/json
          mode: Literal
          literalQuery:
            isPropertiesRef: true
          literalPath:
            pathElements:
            - values:
              - $type: ScriptedValue
              fixedValue:
                $type: StringTestValue
                value: chat-server
            - values:
              - $type: ScriptedValue
              fixedValue:
                $type: StringTestValue
                value: admin
            - values:
              - $type: ScriptedValue
              fixedValue:
                $type: StringTestValue
                value: db
            - values:
              - $type: ScriptedValue
              fixedValue:
                $type: StringTestValue
                value: backup
          resourceMethod:
            resourceId: /admin/db/backup
            httpMethod: GET
          resourceMode: 3
          baseUrl:
            values:
            - $type: ScriptedValue
            - $type: WadlTestValue
              value: http://localhost/chat-server
            fixedValue:
              $type: StringTestValue
              value: http://localhost/chat-server
            selectedIndex: 1
    - $type: TestSuite
      name: /admin/email/config
      testID: 91
      tests:
      - $type: RESTClientToolTest
        name: /admin/email/config - GET
        testID: 92
        performanceGroup: 2
        tool:
          $type: RESTClient
          iconName: RESTClient
          name: /admin/email/config - GET
          formJson:
            value:
              $type: ElementValue
              writeType: true
              hasReference: true
              qnameAsString: :root
              replacedColumn: ""
              values:
              - $type: ComplexValue
                replacedColumn: ""
                attributes:
                - replacedColumn: ""
                  value:
                    $type: StringValue
                    replacedColumn: ""
                    value: object
                  useValue: true
                compositorValue: true
                compositorValueObj:
                  replacedColumn: ""
                  values:
                    $type: CompositorValueSetCollectionSet
                    set:
                    - $type: CompositorValueSet
            elementTypeName: root
          hasServiceInfo: true
          serviceInfo:
            serviceDescriptor:
              $type: StandardServiceDescriptor
              location: file:///D:/Code_Workspace/project/SIM/docs/swagger/sim-api-soatest-openapi.yaml
            serviceName: ""
            versionName: ""
          jsonBuilder:
            hasValue: true
            value:
              $type: JSONObjectValue
              nameIsNull: true
          xmlBuilder: false
          schemaURL:
            MessagingClient_SchemaLocation: file:///D:/Code_Workspace/project/SIM/docs/swagger/sim-api-soatest-openapi.yaml
          formInput:
            value:
              $type: ElementValue
              writeType: true
              hasReference: true
              qnameAsString: ":"
              replacedColumn: ""
              values:
              - $type: ComplexValue
                replacedColumn: ""
                compositorValue: true
                compositorValueObj:
                  replacedColumn: ""
                  values:
                    $type: CompositorValueSetCollectionSet
                    set:
                    - $type: CompositorValueSet
          jmsMessageOutputProvider:
            $type: JMSMessageOutputProvider
            jmsOutputProviderRequest:
              $type: JMSOutputProvider
              name: Request Object
              menuName: Object
            jmsOutputProviderResponse:
              $type: JMSOutputProvider
              name: Response Message Object
              menuName: Message Object
          validResponseRange:
            validResponseRange:
              values:
              - $type: ScriptedValue
              fixedValue:
                $type: StringTestValue
                validResponseRange: 200
          router:
            values:
            - $type: ScriptedValue
            fixedValue:
              $type: StringTestValue
              HTTPClient_Endpoint: http://localhost/chat-server/admin/email/config
          transportProperties:
            manager:
              protocol: 1
              properties:
              - $type: HTTPClientHTTPProperties
                followRedirects:
                  bool: true
                common:
                  method:
                    values:
                    - $type: ScriptedValue
                    fixedValue:
                      $type: HTTPMethodTestValue
                      method: GET
                  httpHeaders:
                    properties:
                    - name: Accept
                      value:
                        values:
                        - $type: ScriptedValue
                        fixedValue:
                          $type: StringTestValue
                          value: application/json
                protocol: 1
                keepAlive1_1:
                  bool: true
            messageExchangePattern:
              inverted: true
          outputProviders:
            requestHeader:
              $type: HTTPNamedToolOutputProvider
              menuName: Transport Header
              name: Request Transport Header
            responseHeader:
              $type: HTTPNamedToolOutputProvider
              menuName: Transport Header
              name: Response Transport Header
            xmlRequestOutput:
              $type: NamedXMLToolOutputProvider
              menuName: Traffic
              name: Request Traffic
            trafficOutput:
              m_name: Traffic Stream
            objectOutput:
              $type: ObjectOutputProvider
              outputTools:
              - $type: TrafficViewer
                iconName: TrafficViewer
                name: Traffic Viewer
                showRequestHeaders: true
              name: Traffic Object
          literal:
            use: 1
            text:
              MessagingClient_LiteralMessage: ""
              type: application/json
          mode: Literal
          literalQuery:
            isPropertiesRef: true
          literalPath:
            pathElements:
            - values:
              - $type: ScriptedValue
              fixedValue:
                $type: StringTestValue
                value: chat-server
            - values:
              - $type: ScriptedValue
              fixedValue:
                $type: StringTestValue
                value: admin
            - values:
              - $type: ScriptedValue
              fixedValue:
                $type: StringTestValue
                value: email
            - values:
              - $type: ScriptedValue
              fixedValue:
                $type: StringTestValue
                value: config
          resourceMethod:
            resourceId: /admin/email/config
            httpMethod: GET
          resourceMode: 3
          baseUrl:
            values:
            - $type: ScriptedValue
            - $type: WadlTestValue
              value: http://localhost/chat-server
            fixedValue:
              $type: StringTestValue
              value: http://localhost/chat-server
            selectedIndex: 1
    - $type: TestSuite
      name: /admin/terms
      testID: 104
      tests:
      - $type: RESTClientToolTest
        name: /admin/terms - GET
        testID: 105
        performanceGroup: 8
        tool:
          $type: RESTClient
          iconName: RESTClient
          name: /admin/terms - GET
          formJson:
            value:
              $type: ElementValue
              writeType: true
              hasReference: true
              qnameAsString: :root
              replacedColumn: ""
              values:
              - $type: ComplexValue
                replacedColumn: ""
                attributes:
                - replacedColumn: ""
                  value:
                    $type: StringValue
                    replacedColumn: ""
                    value: object
                  useValue: true
                compositorValue: true
                compositorValueObj:
                  replacedColumn: ""
                  values:
                    $type: CompositorValueSetCollectionSet
                    set:
                    - $type: CompositorValueSet
            elementTypeName: root
          hasServiceInfo: true
          serviceInfo:
            serviceDescriptor:
              $type: StandardServiceDescriptor
              location: file:///D:/Code_Workspace/project/SIM/docs/swagger/sim-api-soatest-openapi.yaml
            serviceName: ""
            versionName: ""
          jsonBuilder:
            hasValue: true
            value:
              $type: JSONObjectValue
              nameIsNull: true
          xmlBuilder: false
          schemaURL:
            MessagingClient_SchemaLocation: file:///D:/Code_Workspace/project/SIM/docs/swagger/sim-api-soatest-openapi.yaml
          formInput:
            value:
              $type: ElementValue
              writeType: true
              hasReference: true
              qnameAsString: ":"
              replacedColumn: ""
              values:
              - $type: ComplexValue
                replacedColumn: ""
                compositorValue: true
                compositorValueObj:
                  replacedColumn: ""
                  values:
                    $type: CompositorValueSetCollectionSet
                    set:
                    - $type: CompositorValueSet
          jmsMessageOutputProvider:
            $type: JMSMessageOutputProvider
            jmsOutputProviderRequest:
              $type: JMSOutputProvider
              name: Request Object
              menuName: Object
            jmsOutputProviderResponse:
              $type: JMSOutputProvider
              name: Response Message Object
              menuName: Message Object
          validResponseRange:
            validResponseRange:
              values:
              - $type: ScriptedValue
              fixedValue:
                $type: StringTestValue
                validResponseRange: 200
          router:
            values:
            - $type: ScriptedValue
            fixedValue:
              $type: StringTestValue
              HTTPClient_Endpoint: http://localhost/chat-server/admin/terms
          transportProperties:
            manager:
              protocol: 1
              properties:
              - $type: HTTPClientHTTPProperties
                followRedirects:
                  bool: true
                common:
                  method:
                    values:
                    - $type: ScriptedValue
                    fixedValue:
                      $type: HTTPMethodTestValue
                      method: GET
                  httpHeaders:
                    properties:
                    - name: Accept
                      value:
                        values:
                        - $type: ScriptedValue
                        fixedValue:
                          $type: StringTestValue
                          value: application/json
                protocol: 1
                keepAlive1_1:
                  bool: true
            messageExchangePattern:
              inverted: true
          outputProviders:
            requestHeader:
              $type: HTTPNamedToolOutputProvider
              menuName: Transport Header
              name: Request Transport Header
            responseHeader:
              $type: HTTPNamedToolOutputProvider
              menuName: Transport Header
              name: Response Transport Header
            xmlRequestOutput:
              $type: NamedXMLToolOutputProvider
              menuName: Traffic
              name: Request Traffic
            trafficOutput:
              m_name: Traffic Stream
            objectOutput:
              $type: ObjectOutputProvider
              outputTools:
              - $type: TrafficViewer
                iconName: TrafficViewer
                name: Traffic Viewer
                showRequestHeaders: true
              name: Traffic Object
          literal:
            use: 1
            text:
              MessagingClient_LiteralMessage: ""
              type: application/json
          mode: Literal
          literalQuery:
            isPropertiesRef: true
          literalPath:
            pathElements:
            - values:
              - $type: ScriptedValue
              fixedValue:
                $type: StringTestValue
                value: chat-server
            - values:
              - $type: ScriptedValue
              fixedValue:
                $type: StringTestValue
                value: admin
            - values:
              - $type: ScriptedValue
              fixedValue:
                $type: StringTestValue
                value: terms
          resourceMethod:
            resourceId: /admin/terms
            httpMethod: GET
          resourceMode: 3
          baseUrl:
            values:
            - $type: ScriptedValue
            - $type: WadlTestValue
              value: http://localhost/chat-server
            fixedValue:
              $type: StringTestValue
              value: http://localhost/chat-server
            selectedIndex: 1
    - $type: TestSuite
      name: /admin/users
      testID: 117
      tests:
      - $type: RESTClientToolTest
        name: /admin/users - GET
        testID: 118
        performanceGroup: 13
        tool:
          $type: RESTClient
          iconName: RESTClient
          name: /admin/users - GET
          formJson:
            value:
              $type: ElementValue
              writeType: true
              hasReference: true
              qnameAsString: :root
              replacedColumn: ""
              values:
              - $type: ComplexValue
                replacedColumn: ""
                attributes:
                - replacedColumn: ""
                  value:
                    $type: StringValue
                    replacedColumn: ""
                    value: object
                  useValue: true
                compositorValue: true
                compositorValueObj:
                  replacedColumn: ""
                  values:
                    $type: CompositorValueSetCollectionSet
                    set:
                    - $type: CompositorValueSet
            elementTypeName: root
          hasServiceInfo: true
          serviceInfo:
            serviceDescriptor:
              $type: StandardServiceDescriptor
              location: file:///D:/Code_Workspace/project/SIM/docs/swagger/sim-api-soatest-openapi.yaml
            serviceName: ""
            versionName: ""
          jsonBuilder:
            hasValue: true
            value:
              $type: JSONObjectValue
              nameIsNull: true
          xmlBuilder: false
          schemaURL:
            MessagingClient_SchemaLocation: file:///D:/Code_Workspace/project/SIM/docs/swagger/sim-api-soatest-openapi.yaml
          formInput:
            value:
              $type: ElementValue
              writeType: true
              hasReference: true
              qnameAsString: ":"
              replacedColumn: ""
              values:
              - $type: ComplexValue
                replacedColumn: ""
                compositorValue: true
                compositorValueObj:
                  replacedColumn: ""
                  values:
                    $type: CompositorValueSetCollectionSet
                    set:
                    - $type: CompositorValueSet
          jmsMessageOutputProvider:
            $type: JMSMessageOutputProvider
            jmsOutputProviderRequest:
              $type: JMSOutputProvider
              name: Request Object
              menuName: Object
            jmsOutputProviderResponse:
              $type: JMSOutputProvider
              name: Response Message Object
              menuName: Message Object
          validResponseRange:
            validResponseRange:
              values:
              - $type: ScriptedValue
              fixedValue:
                $type: StringTestValue
                validResponseRange: 200
          router:
            values:
            - $type: ScriptedValue
            fixedValue:
              $type: StringTestValue
              HTTPClient_Endpoint: http://localhost/chat-server/admin/users
          transportProperties:
            manager:
              protocol: 1
              properties:
              - $type: HTTPClientHTTPProperties
                followRedirects:
                  bool: true
                common:
                  method:
                    values:
                    - $type: ScriptedValue
                    fixedValue:
                      $type: HTTPMethodTestValue
                      method: GET
                  httpHeaders:
                    properties:
                    - name: Accept
                      value:
                        values:
                        - $type: ScriptedValue
                        fixedValue:
                          $type: StringTestValue
                          value: application/json
                protocol: 1
                keepAlive1_1:
                  bool: true
            messageExchangePattern:
              inverted: true
          outputProviders:
            requestHeader:
              $type: HTTPNamedToolOutputProvider
              menuName: Transport Header
              name: Request Transport Header
            responseHeader:
              $type: HTTPNamedToolOutputProvider
              menuName: Transport Header
              name: Response Transport Header
            xmlRequestOutput:
              $type: NamedXMLToolOutputProvider
              menuName: Traffic
              name: Request Traffic
            trafficOutput:
              m_name: Traffic Stream
            objectOutput:
              $type: ObjectOutputProvider
              outputTools:
              - $type: TrafficViewer
                iconName: TrafficViewer
                name: Traffic Viewer
                showRequestHeaders: true
              name: Traffic Object
          literal:
            use: 1
            text:
              MessagingClient_LiteralMessage: ""
              type: application/json
          mode: Literal
          literalQuery:
            isPropertiesRef: true
          literalPath:
            pathElements:
            - values:
              - $type: ScriptedValue
              fixedValue:
                $type: StringTestValue
                value: chat-server
            - values:
              - $type: ScriptedValue
              fixedValue:
                $type: StringTestValue
                value: admin
            - values:
              - $type: ScriptedValue
              fixedValue:
                $type: StringTestValue
                value: users
          resourceMethod:
            resourceId: /admin/users
            httpMethod: GET
          resourceMode: 3
          baseUrl:
            values:
            - $type: ScriptedValue
            - $type: WadlTestValue
              value: http://localhost/chat-server
            fixedValue:
              $type: StringTestValue
              value: http://localhost/chat-server
            selectedIndex: 1
    - $type: TestSuite
      name: /admin/widget-health-config
      testID: 121
      tests:
      - $type: RESTClientToolTest
        name: /admin/widget-health-config - GET
        testID: 122
        performanceGroup: 14
        tool:
          $type: RESTClient
          iconName: RESTClient
          name: /admin/widget-health-config - GET
          formJson:
            value:
              $type: ElementValue
              writeType: true
              hasReference: true
              qnameAsString: :root
              replacedColumn: ""
              values:
              - $type: ComplexValue
                replacedColumn: ""
                attributes:
                - replacedColumn: ""
                  value:
                    $type: StringValue
                    replacedColumn: ""
                    value: object
                  useValue: true
                compositorValue: true
                compositorValueObj:
                  replacedColumn: ""
                  values:
                    $type: CompositorValueSetCollectionSet
                    set:
                    - $type: CompositorValueSet
            elementTypeName: root
          hasServiceInfo: true
          serviceInfo:
            serviceDescriptor:
              $type: StandardServiceDescriptor
              location: file:///D:/Code_Workspace/project/SIM/docs/swagger/sim-api-soatest-openapi.yaml
            serviceName: ""
            versionName: ""
          jsonBuilder:
            hasValue: true
            value:
              $type: JSONObjectValue
              nameIsNull: true
          xmlBuilder: false
          schemaURL:
            MessagingClient_SchemaLocation: file:///D:/Code_Workspace/project/SIM/docs/swagger/sim-api-soatest-openapi.yaml
          formInput:
            value:
              $type: ElementValue
              writeType: true
              hasReference: true
              qnameAsString: ":"
              replacedColumn: ""
              values:
              - $type: ComplexValue
                replacedColumn: ""
                compositorValue: true
                compositorValueObj:
                  replacedColumn: ""
                  values:
                    $type: CompositorValueSetCollectionSet
                    set:
                    - $type: CompositorValueSet
          jmsMessageOutputProvider:
            $type: JMSMessageOutputProvider
            jmsOutputProviderRequest:
              $type: JMSOutputProvider
              name: Request Object
              menuName: Object
            jmsOutputProviderResponse:
              $type: JMSOutputProvider
              name: Response Message Object
              menuName: Message Object
          validResponseRange:
            validResponseRange:
              values:
              - $type: ScriptedValue
              fixedValue:
                $type: StringTestValue
                validResponseRange: 200
          router:
            values:
            - $type: ScriptedValue
            fixedValue:
              $type: StringTestValue
              HTTPClient_Endpoint: http://localhost/chat-server/admin/widget-health-config
          transportProperties:
            manager:
              protocol: 1
              properties:
              - $type: HTTPClientHTTPProperties
                followRedirects:
                  bool: true
                common:
                  method:
                    values:
                    - $type: ScriptedValue
                    fixedValue:
                      $type: HTTPMethodTestValue
                      method: GET
                  httpHeaders:
                    properties:
                    - name: Accept
                      value:
                        values:
                        - $type: ScriptedValue
                        fixedValue:
                          $type: StringTestValue
                          value: application/json
                protocol: 1
                keepAlive1_1:
                  bool: true
            messageExchangePattern:
              inverted: true
          outputProviders:
            requestHeader:
              $type: HTTPNamedToolOutputProvider
              menuName: Transport Header
              name: Request Transport Header
            responseHeader:
              $type: HTTPNamedToolOutputProvider
              menuName: Transport Header
              name: Response Transport Header
            xmlRequestOutput:
              $type: NamedXMLToolOutputProvider
              menuName: Traffic
              name: Request Traffic
            trafficOutput:
              m_name: Traffic Stream
            objectOutput:
              $type: ObjectOutputProvider
              outputTools:
              - $type: TrafficViewer
                iconName: TrafficViewer
                name: Traffic Viewer
                showRequestHeaders: true
              name: Traffic Object
          literal:
            use: 1
            text:
              MessagingClient_LiteralMessage: ""
              type: application/json
          mode: Literal
          literalQuery:
            isPropertiesRef: true
          literalPath:
            pathElements:
            - values:
              - $type: ScriptedValue
              fixedValue:
                $type: StringTestValue
                value: chat-server
            - values:
              - $type: ScriptedValue
              fixedValue:
                $type: StringTestValue
                value: admin
            - values:
              - $type: ScriptedValue
              fixedValue:
                $type: StringTestValue
                value: widget-health-config
          resourceMethod:
            resourceId: /admin/widget-health-config
            httpMethod: GET
          resourceMode: 3
          baseUrl:
            values:
            - $type: ScriptedValue
            - $type: WadlTestValue
              value: http://localhost/chat-server
            fixedValue:
              $type: StringTestValue
              value: http://localhost/chat-server
            selectedIndex: 1
    - $type: TestSuite
      name: /admin/widgets
      testID: 124
      tests:
      - $type: RESTClientToolTest
        name: /admin/widgets - GET
        testID: 125
        performanceGroup: 15
        tool:
          $type: RESTClient
          iconName: RESTClient
          name: /admin/widgets - GET
          formJson:
            value:
              $type: ElementValue
              writeType: true
              hasReference: true
              qnameAsString: :root
              replacedColumn: ""
              values:
              - $type: ComplexValue
                replacedColumn: ""
                attributes:
                - replacedColumn: ""
                  value:
                    $type: StringValue
                    replacedColumn: ""
                    value: object
                  useValue: true
                compositorValue: true
                compositorValueObj:
                  replacedColumn: ""
                  values:
                    $type: CompositorValueSetCollectionSet
                    set:
                    - $type: CompositorValueSet
            elementTypeName: root
          hasServiceInfo: true
          serviceInfo:
            serviceDescriptor:
              $type: StandardServiceDescriptor
              location: file:///D:/Code_Workspace/project/SIM/docs/swagger/sim-api-soatest-openapi.yaml
            serviceName: ""
            versionName: ""
          jsonBuilder:
            hasValue: true
            value:
              $type: JSONObjectValue
              nameIsNull: true
          xmlBuilder: false
          schemaURL:
            MessagingClient_SchemaLocation: file:///D:/Code_Workspace/project/SIM/docs/swagger/sim-api-soatest-openapi.yaml
          formInput:
            value:
              $type: ElementValue
              writeType: true
              hasReference: true
              qnameAsString: ":"
              replacedColumn: ""
              values:
              - $type: ComplexValue
                replacedColumn: ""
                compositorValue: true
                compositorValueObj:
                  replacedColumn: ""
                  values:
                    $type: CompositorValueSetCollectionSet
                    set:
                    - $type: CompositorValueSet
          jmsMessageOutputProvider:
            $type: JMSMessageOutputProvider
            jmsOutputProviderRequest:
              $type: JMSOutputProvider
              name: Request Object
              menuName: Object
            jmsOutputProviderResponse:
              $type: JMSOutputProvider
              name: Response Message Object
              menuName: Message Object
          validResponseRange:
            validResponseRange:
              values:
              - $type: ScriptedValue
              fixedValue:
                $type: StringTestValue
                validResponseRange: 200
          router:
            values:
            - $type: ScriptedValue
            fixedValue:
              $type: StringTestValue
              HTTPClient_Endpoint: http://localhost/chat-server/admin/widgets
          transportProperties:
            manager:
              protocol: 1
              properties:
              - $type: HTTPClientHTTPProperties
                followRedirects:
                  bool: true
                common:
                  method:
                    values:
                    - $type: ScriptedValue
                    fixedValue:
                      $type: HTTPMethodTestValue
                      method: GET
                  httpHeaders:
                    properties:
                    - name: Accept
                      value:
                        values:
                        - $type: ScriptedValue
                        fixedValue:
                          $type: StringTestValue
                          value: application/json
                protocol: 1
                keepAlive1_1:
                  bool: true
            messageExchangePattern:
              inverted: true
          outputProviders:
            requestHeader:
              $type: HTTPNamedToolOutputProvider
              menuName: Transport Header
              name: Request Transport Header
            responseHeader:
              $type: HTTPNamedToolOutputProvider
              menuName: Transport Header
              name: Response Transport Header
            xmlRequestOutput:
              $type: NamedXMLToolOutputProvider
              menuName: Traffic
              name: Request Traffic
            trafficOutput:
              m_name: Traffic Stream
            objectOutput:
              $type: ObjectOutputProvider
              outputTools:
              - $type: TrafficViewer
                iconName: TrafficViewer
                name: Traffic Viewer
                showRequestHeaders: true
              name: Traffic Object
          literal:
            use: 1
            text:
              MessagingClient_LiteralMessage: ""
              type: application/json
          mode: Literal
          literalQuery:
            isPropertiesRef: true
          literalPath:
            pathElements:
            - values:
              - $type: ScriptedValue
              fixedValue:
                $type: StringTestValue
                value: chat-server
            - values:
              - $type: ScriptedValue
              fixedValue:
                $type: StringTestValue
                value: admin
            - values:
              - $type: ScriptedValue
              fixedValue:
                $type: StringTestValue
                value: widgets
          resourceMethod:
            resourceId: /admin/widgets
            httpMethod: GET
          resourceMode: 3
          baseUrl:
            values:
            - $type: ScriptedValue
            - $type: WadlTestValue
              value: http://localhost/chat-server
            fixedValue:
              $type: StringTestValue
              value: http://localhost/chat-server
            selectedIndex: 1
    - $type: TestSuite
      name: /admin/widgets/sync/timer
      testID: 131
      tests:
      - $type: RESTClientToolTest
        name: /admin/widgets/sync/timer - GET
        testID: 132
        performanceGroup: 17
        tool:
          $type: RESTClient
          iconName: RESTClient
          name: /admin/widgets/sync/timer - GET
          formJson:
            value:
              $type: ElementValue
              writeType: true
              hasReference: true
              qnameAsString: :root
              replacedColumn: ""
              values:
              - $type: ComplexValue
                replacedColumn: ""
                attributes:
                - replacedColumn: ""
                  value:
                    $type: StringValue
                    replacedColumn: ""
                    value: object
                  useValue: true
                compositorValue: true
                compositorValueObj:
                  replacedColumn: ""
                  values:
                    $type: CompositorValueSetCollectionSet
                    set:
                    - $type: CompositorValueSet
            elementTypeName: root
          hasServiceInfo: true
          serviceInfo:
            serviceDescriptor:
              $type: StandardServiceDescriptor
              location: file:///D:/Code_Workspace/project/SIM/docs/swagger/sim-api-soatest-openapi.yaml
            serviceName: ""
            versionName: ""
          jsonBuilder:
            hasValue: true
            value:
              $type: JSONObjectValue
              nameIsNull: true
          xmlBuilder: false
          schemaURL:
            MessagingClient_SchemaLocation: file:///D:/Code_Workspace/project/SIM/docs/swagger/sim-api-soatest-openapi.yaml
          formInput:
            value:
              $type: ElementValue
              writeType: true
              hasReference: true
              qnameAsString: ":"
              replacedColumn: ""
              values:
              - $type: ComplexValue
                replacedColumn: ""
                compositorValue: true
                compositorValueObj:
                  replacedColumn: ""
                  values:
                    $type: CompositorValueSetCollectionSet
                    set:
                    - $type: CompositorValueSet
          jmsMessageOutputProvider:
            $type: JMSMessageOutputProvider
            jmsOutputProviderRequest:
              $type: JMSOutputProvider
              name: Request Object
              menuName: Object
            jmsOutputProviderResponse:
              $type: JMSOutputProvider
              name: Response Message Object
              menuName: Message Object
          validResponseRange:
            validResponseRange:
              values:
              - $type: ScriptedValue
              fixedValue:
                $type: StringTestValue
                validResponseRange: 200
          router:
            values:
            - $type: ScriptedValue
            fixedValue:
              $type: StringTestValue
              HTTPClient_Endpoint: http://localhost/chat-server/admin/widgets/sync/timer
          transportProperties:
            manager:
              protocol: 1
              properties:
              - $type: HTTPClientHTTPProperties
                followRedirects:
                  bool: true
                common:
                  method:
                    values:
                    - $type: ScriptedValue
                    fixedValue:
                      $type: HTTPMethodTestValue
                      method: GET
                  httpHeaders:
                    properties:
                    - name: Accept
                      value:
                        values:
                        - $type: ScriptedValue
                        fixedValue:
                          $type: StringTestValue
                          value: application/json
                protocol: 1
                keepAlive1_1:
                  bool: true
            messageExchangePattern:
              inverted: true
          outputProviders:
            requestHeader:
              $type: HTTPNamedToolOutputProvider
              menuName: Transport Header
              name: Request Transport Header
            responseHeader:
              $type: HTTPNamedToolOutputProvider
              menuName: Transport Header
              name: Response Transport Header
            xmlRequestOutput:
              $type: NamedXMLToolOutputProvider
              menuName: Traffic
              name: Request Traffic
            trafficOutput:
              m_name: Traffic Stream
            objectOutput:
              $type: ObjectOutputProvider
              outputTools:
              - $type: TrafficViewer
                iconName: TrafficViewer
                name: Traffic Viewer
                showRequestHeaders: true
              name: Traffic Object
          literal:
            use: 1
            text:
              MessagingClient_LiteralMessage: ""
              type: application/json
          mode: Literal
          literalQuery:
            isPropertiesRef: true
          literalPath:
            pathElements:
            - values:
              - $type: ScriptedValue
              fixedValue:
                $type: StringTestValue
                value: chat-server
            - values:
              - $type: ScriptedValue
              fixedValue:
                $type: StringTestValue
                value: admin
            - values:
              - $type: ScriptedValue
              fixedValue:
                $type: StringTestValue
                value: widgets
            - values:
              - $type: ScriptedValue
              fixedValue:
                $type: StringTestValue
                value: sync
            - values:
              - $type: ScriptedValue
              fixedValue:
                $type: StringTestValue
                value: timer
          resourceMethod:
            resourceId: /admin/widgets/sync/timer
            httpMethod: GET
          resourceMode: 3
          baseUrl:
            values:
            - $type: ScriptedValue
            - $type: WadlTestValue
              value: http://localhost/chat-server
            fixedValue:
              $type: StringTestValue
              value: http://localhost/chat-server
            selectedIndex: 1
    - $type: TestSuite
      name: /dashboard/sessions/data
      testID: 142
      tests:
      - $type: RESTClientToolTest
        name: /dashboard/sessions/data - GET
        testID: 143
        performanceGroup: 22
        tool:
          $type: RESTClient
          iconName: RESTClient
          name: /dashboard/sessions/data - GET
          formJson:
            value:
              $type: ElementValue
              writeType: true
              hasReference: true
              qnameAsString: :root
              replacedColumn: ""
              values:
              - $type: ComplexValue
                replacedColumn: ""
                attributes:
                - replacedColumn: ""
                  value:
                    $type: StringValue
                    replacedColumn: ""
                    value: object
                  useValue: true
                compositorValue: true
                compositorValueObj:
                  replacedColumn: ""
                  values:
                    $type: CompositorValueSetCollectionSet
                    set:
                    - $type: CompositorValueSet
            elementTypeName: root
          hasServiceInfo: true
          serviceInfo:
            serviceDescriptor:
              $type: StandardServiceDescriptor
              location: file:///D:/Code_Workspace/project/SIM/docs/swagger/sim-api-soatest-openapi.yaml
            serviceName: ""
            versionName: ""
          jsonBuilder:
            hasValue: true
            value:
              $type: JSONObjectValue
              nameIsNull: true
          xmlBuilder: false
          schemaURL:
            MessagingClient_SchemaLocation: file:///D:/Code_Workspace/project/SIM/docs/swagger/sim-api-soatest-openapi.yaml
          formInput:
            value:
              $type: ElementValue
              writeType: true
              hasReference: true
              qnameAsString: ":"
              replacedColumn: ""
              values:
              - $type: ComplexValue
                replacedColumn: ""
                compositorValue: true
                compositorValueObj:
                  replacedColumn: ""
                  values:
                    $type: CompositorValueSetCollectionSet
                    set:
                    - $type: CompositorValueSet
          jmsMessageOutputProvider:
            $type: JMSMessageOutputProvider
            jmsOutputProviderRequest:
              $type: JMSOutputProvider
              name: Request Object
              menuName: Object
            jmsOutputProviderResponse:
              $type: JMSOutputProvider
              name: Response Message Object
              menuName: Message Object
          validResponseRange:
            validResponseRange:
              values:
              - $type: ScriptedValue
              fixedValue:
                $type: StringTestValue
                validResponseRange: 200
          router:
            values:
            - $type: ScriptedValue
            fixedValue:
              $type: StringTestValue
              HTTPClient_Endpoint: http://localhost/chat-server/dashboard/sessions/data
          transportProperties:
            manager:
              protocol: 1
              properties:
              - $type: HTTPClientHTTPProperties
                followRedirects:
                  bool: true
                common:
                  method:
                    values:
                    - $type: ScriptedValue
                    fixedValue:
                      $type: HTTPMethodTestValue
                      method: GET
                  httpHeaders:
                    properties:
                    - name: Accept
                      value:
                        values:
                        - $type: ScriptedValue
                        fixedValue:
                          $type: StringTestValue
                          value: application/json
                protocol: 1
                keepAlive1_1:
                  bool: true
            messageExchangePattern:
              inverted: true
          outputProviders:
            requestHeader:
              $type: HTTPNamedToolOutputProvider
              menuName: Transport Header
              name: Request Transport Header
            responseHeader:
              $type: HTTPNamedToolOutputProvider
              menuName: Transport Header
              name: Response Transport Header
            xmlRequestOutput:
              $type: NamedXMLToolOutputProvider
              menuName: Traffic
              name: Request Traffic
            trafficOutput:
              m_name: Traffic Stream
            objectOutput:
              $type: ObjectOutputProvider
              outputTools:
              - $type: TrafficViewer
                iconName: TrafficViewer
                name: Traffic Viewer
                showRequestHeaders: true
              name: Traffic Object
          literal:
            use: 1
            text:
              MessagingClient_LiteralMessage: ""
              type: application/json
          mode: Literal
          literalQuery:
            isPropertiesRef: true
          literalPath:
            pathElements:
            - values:
              - $type: ScriptedValue
              fixedValue:
                $type: StringTestValue
                value: chat-server
            - values:
              - $type: ScriptedValue
              fixedValue:
                $type: StringTestValue
                value: dashboard
            - values:
              - $type: ScriptedValue
              fixedValue:
                $type: StringTestValue
                value: sessions
            - values:
              - $type: ScriptedValue
              fixedValue:
                $type: StringTestValue
                value: data
          resourceMethod:
            resourceId: /dashboard/sessions/data
            httpMethod: GET
          resourceMode: 3
          baseUrl:
            values:
            - $type: ScriptedValue
            - $type: WadlTestValue
              value: http://localhost/chat-server
            fixedValue:
              $type: StringTestValue
              value: http://localhost/chat-server
            selectedIndex: 1
    - $type: TestSuite
      name: /dashboard/sessions/select
      testID: 145
      tests:
      - $type: RESTClientToolTest
        name: /dashboard/sessions/select - GET
        testID: 146
        performanceGroup: 23
        tool:
          $type: RESTClient
          iconName: RESTClient
          name: /dashboard/sessions/select - GET
          formJson:
            value:
              $type: ElementValue
              writeType: true
              hasReference: true
              qnameAsString: :root
              replacedColumn: ""
              values:
              - $type: ComplexValue
                replacedColumn: ""
                attributes:
                - replacedColumn: ""
                  value:
                    $type: StringValue
                    replacedColumn: ""
                    value: object
                  useValue: true
                compositorValue: true
                compositorValueObj:
                  replacedColumn: ""
                  values:
                    $type: CompositorValueSetCollectionSet
                    set:
                    - $type: CompositorValueSet
            elementTypeName: root
          hasServiceInfo: true
          serviceInfo:
            serviceDescriptor:
              $type: StandardServiceDescriptor
              location: file:///D:/Code_Workspace/project/SIM/docs/swagger/sim-api-soatest-openapi.yaml
            serviceName: ""
            versionName: ""
          jsonBuilder:
            hasValue: true
            value:
              $type: JSONObjectValue
              nameIsNull: true
          xmlBuilder: false
          schemaURL:
            MessagingClient_SchemaLocation: file:///D:/Code_Workspace/project/SIM/docs/swagger/sim-api-soatest-openapi.yaml
          formInput:
            value:
              $type: ElementValue
              writeType: true
              hasReference: true
              qnameAsString: ":"
              replacedColumn: ""
              values:
              - $type: ComplexValue
                replacedColumn: ""
                compositorValue: true
                compositorValueObj:
                  replacedColumn: ""
                  values:
                    $type: CompositorValueSetCollectionSet
                    set:
                    - $type: CompositorValueSet
          jmsMessageOutputProvider:
            $type: JMSMessageOutputProvider
            jmsOutputProviderRequest:
              $type: JMSOutputProvider
              name: Request Object
              menuName: Object
            jmsOutputProviderResponse:
              $type: JMSOutputProvider
              name: Response Message Object
              menuName: Message Object
          validResponseRange:
            validResponseRange:
              values:
              - $type: ScriptedValue
              fixedValue:
                $type: StringTestValue
                validResponseRange: 200
          router:
            values:
            - $type: ScriptedValue
            fixedValue:
              $type: StringTestValue
              HTTPClient_Endpoint: http://localhost/chat-server/dashboard/sessions/select
          transportProperties:
            manager:
              protocol: 1
              properties:
              - $type: HTTPClientHTTPProperties
                followRedirects:
                  bool: true
                common:
                  method:
                    values:
                    - $type: ScriptedValue
                    fixedValue:
                      $type: HTTPMethodTestValue
                      method: GET
                  httpHeaders:
                    properties:
                    - name: Accept
                      value:
                        values:
                        - $type: ScriptedValue
                        fixedValue:
                          $type: StringTestValue
                          value: application/json
                protocol: 1
                keepAlive1_1:
                  bool: true
            messageExchangePattern:
              inverted: true
          outputProviders:
            requestHeader:
              $type: HTTPNamedToolOutputProvider
              menuName: Transport Header
              name: Request Transport Header
            responseHeader:
              $type: HTTPNamedToolOutputProvider
              menuName: Transport Header
              name: Response Transport Header
            xmlRequestOutput:
              $type: NamedXMLToolOutputProvider
              menuName: Traffic
              name: Request Traffic
            trafficOutput:
              m_name: Traffic Stream
            objectOutput:
              $type: ObjectOutputProvider
              outputTools:
              - $type: TrafficViewer
                iconName: TrafficViewer
                name: Traffic Viewer
                showRequestHeaders: true
              name: Traffic Object
          literal:
            use: 1
            text:
              MessagingClient_LiteralMessage: ""
              type: application/json
          mode: Literal
          literalQuery:
            isPropertiesRef: true
          literalPath:
            pathElements:
            - values:
              - $type: ScriptedValue
              fixedValue:
                $type: StringTestValue
                value: chat-server
            - values:
              - $type: ScriptedValue
              fixedValue:
                $type: StringTestValue
                value: dashboard
            - values:
              - $type: ScriptedValue
              fixedValue:
                $type: StringTestValue
                value: sessions
            - values:
              - $type: ScriptedValue
              fixedValue:
                $type: StringTestValue
                value: select
          resourceMethod:
            resourceId: /dashboard/sessions/select
            httpMethod: GET
          resourceMode: 3
          baseUrl:
            values:
            - $type: ScriptedValue
            - $type: WadlTestValue
              value: http://localhost/chat-server
            fixedValue:
              $type: StringTestValue
              value: http://localhost/chat-server
            selectedIndex: 1
    - $type: TestSuite
      name: /dashboard/topics/data
      testID: 148
      tests:
      - $type: RESTClientToolTest
        name: /dashboard/topics/data - GET
        testID: 149
        performanceGroup: 24
        tool:
          $type: RESTClient
          iconName: RESTClient
          name: /dashboard/topics/data - GET
          formJson:
            value:
              $type: ElementValue
              writeType: true
              hasReference: true
              qnameAsString: :root
              replacedColumn: ""
              values:
              - $type: ComplexValue
                replacedColumn: ""
                attributes:
                - replacedColumn: ""
                  value:
                    $type: StringValue
                    replacedColumn: ""
                    value: object
                  useValue: true
                compositorValue: true
                compositorValueObj:
                  replacedColumn: ""
                  values:
                    $type: CompositorValueSetCollectionSet
                    set:
                    - $type: CompositorValueSet
            elementTypeName: root
          hasServiceInfo: true
          serviceInfo:
            serviceDescriptor:
              $type: StandardServiceDescriptor
              location: file:///D:/Code_Workspace/project/SIM/docs/swagger/sim-api-soatest-openapi.yaml
            serviceName: ""
            versionName: ""
          jsonBuilder:
            hasValue: true
            value:
              $type: JSONObjectValue
              nameIsNull: true
          xmlBuilder: false
          schemaURL:
            MessagingClient_SchemaLocation: file:///D:/Code_Workspace/project/SIM/docs/swagger/sim-api-soatest-openapi.yaml
          formInput:
            value:
              $type: ElementValue
              writeType: true
              hasReference: true
              qnameAsString: ":"
              replacedColumn: ""
              values:
              - $type: ComplexValue
                replacedColumn: ""
                compositorValue: true
                compositorValueObj:
                  replacedColumn: ""
                  values:
                    $type: CompositorValueSetCollectionSet
                    set:
                    - $type: CompositorValueSet
          jmsMessageOutputProvider:
            $type: JMSMessageOutputProvider
            jmsOutputProviderRequest:
              $type: JMSOutputProvider
              name: Request Object
              menuName: Object
            jmsOutputProviderResponse:
              $type: JMSOutputProvider
              name: Response Message Object
              menuName: Message Object
          validResponseRange:
            validResponseRange:
              values:
              - $type: ScriptedValue
              fixedValue:
                $type: StringTestValue
                validResponseRange: 200
          router:
            values:
            - $type: ScriptedValue
            fixedValue:
              $type: StringTestValue
              HTTPClient_Endpoint: http://localhost/chat-server/dashboard/topics/data
          transportProperties:
            manager:
              protocol: 1
              properties:
              - $type: HTTPClientHTTPProperties
                followRedirects:
                  bool: true
                common:
                  method:
                    values:
                    - $type: ScriptedValue
                    fixedValue:
                      $type: HTTPMethodTestValue
                      method: GET
                  httpHeaders:
                    properties:
                    - name: Accept
                      value:
                        values:
                        - $type: ScriptedValue
                        fixedValue:
                          $type: StringTestValue
                          value: application/json
                protocol: 1
                keepAlive1_1:
                  bool: true
            messageExchangePattern:
              inverted: true
          outputProviders:
            requestHeader:
              $type: HTTPNamedToolOutputProvider
              menuName: Transport Header
              name: Request Transport Header
            responseHeader:
              $type: HTTPNamedToolOutputProvider
              menuName: Transport Header
              name: Response Transport Header
            xmlRequestOutput:
              $type: NamedXMLToolOutputProvider
              menuName: Traffic
              name: Request Traffic
            trafficOutput:
              m_name: Traffic Stream
            objectOutput:
              $type: ObjectOutputProvider
              outputTools:
              - $type: TrafficViewer
                iconName: TrafficViewer
                name: Traffic Viewer
                showRequestHeaders: true
              name: Traffic Object
          literal:
            use: 1
            text:
              MessagingClient_LiteralMessage: ""
              type: application/json
          mode: Literal
          literalQuery:
            isPropertiesRef: true
          literalPath:
            pathElements:
            - values:
              - $type: ScriptedValue
              fixedValue:
                $type: StringTestValue
                value: chat-server
            - values:
              - $type: ScriptedValue
              fixedValue:
                $type: StringTestValue
                value: dashboard
            - values:
              - $type: ScriptedValue
              fixedValue:
                $type: StringTestValue
                value: topics
            - values:
              - $type: ScriptedValue
              fixedValue:
                $type: StringTestValue
                value: data
          resourceMethod:
            resourceId: /dashboard/topics/data
            httpMethod: GET
          resourceMode: 3
          baseUrl:
            values:
            - $type: ScriptedValue
            - $type: WadlTestValue
              value: http://localhost/chat-server
            fixedValue:
              $type: StringTestValue
              value: http://localhost/chat-server
            selectedIndex: 1

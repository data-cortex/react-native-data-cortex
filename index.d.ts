declare module "react-native-data-cortex" {

  namespace DataCortexApi {
    export interface LogProps {
      log_line: string,

      hostname?: string,
      filename?: string,
      log_level?: string,
      device_tag?: string,
      user_tag?: string,
      device_type?: string,
      os?: string,
      os_ver?: string,
      browser?: string,
      browser_ver?: string,
      country?: string,
      language?: string,
      response_ms?: number,
      response_bytes?: number,
    }

    export interface EventProps {
      kingdom?: string,
      phylum?: string,
      class?: string,
      order?: string,
      family?: string,
      genus?: string,
      species?: string,
      float1?: number,
      float2?: number,
      float3?: number,
      float4?: number,
    }

    export interface EconomyProps {
      spendCurrency: string,
      spendAmount: number,
      spendType?: string,
      spend_type?: string,

      kingdom?: string,
      phylum?: string,
      class?: string,
      order?: string,
      family?: string,
      genus?: string,
      species?: string,
      float1?: number,
      float2?: number,
      float3?: number,
      float4?: number,
    }

    function init(api_key: string,org_name: string): void
    function addUserTag(user_tag: string): void
    function event(props: EventProps): void
    function economyEvent(props: EconomyProps): void
    function log(...args: any[]): void
    function logEvent(props: LogProps): void
  }

  export default DataCortexApi
}

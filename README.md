
# react-native-data-cortex

`
target 'Target' do
  post_install do |installer|
    installer.pods_project.targets.each do |target|
      if target.name == "RCTDataCortex"
        target.build_configurations.each do |config|
          config.build_settings['GCC_PREPROCESSOR_DEFINITIONS'] ||= ['$(inherited)']
          config.build_settings['GCC_PREPROCESSOR_DEFINITIONS'] << 'DC_NO_IDFA=1'
        end
        puts "\nInject a macro DC_NO_IDFA to target!\n"
      end
    end
  end
end
`

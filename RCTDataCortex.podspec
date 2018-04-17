
Pod::Spec.new do |s|
  s.name         = "RCTDataCortex"
  s.version      = "0.0.19"
  s.summary      = "RCTDataCortex"
  s.description  = <<-DESC
                  React Native Data Cortex wrapper
                   DESC
  s.homepage     = "https://github.com/data-cortex/react-native-data-cortex"
  s.license      = "MIT"
  s.author       = { "author" => "jim@blueskylabs.com" }
  s.platform     = :ios, "7.0"
  s.source       = { :git => "https://github.com/data-cortex/react-native-data-cortex.git", :tag => "master" }
  s.source_files = "*.{h,m}"
  s.requires_arc = true

  s.dependency "React"

end

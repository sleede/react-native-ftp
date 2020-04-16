require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "RNFtp"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.description  = <<-DESC
                  React Native FTP
                   DESC
  s.homepage     = "https://github.com/sleede/react-native-ftp"
  s.license      = "MIT"
  s.license      = { :type => "MIT", :file => "LICENSE" }
  s.authors      = { "Du Peng" => "peng@sleede.com" }
  s.platforms    = { :ios => "9.0" }
  s.source       = { :git => "https://github.com/sleede/react-native-ftp.git", :tag => "master" }

  s.source_files = "ios/**/*.{h,m,c}"
  s.requires_arc = true

  s.dependency "React"
end

#import "RNFtp.h"
#import "FTPKit/FTPKit.h"
#import <React/RCTConvert.h>
#import <React/RCTLog.h>

@interface RNFtp ()

@property (nonatomic, strong) FTPClient *client;

@end

@implementation RNFtp

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}


RCT_EXPORT_MODULE()

RCT_EXPORT_METHOD(connect:(NSDictionary *)config connectWithResolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    NSString *hostname = [RCTConvert NSString:config[@"hostname"]];
    NSString *username = [RCTConvert NSString:config[@"username"]];
    NSString *password = [RCTConvert NSString:config[@"password"]];

    if (hostname == nil || hostname.length <= 0)
    {
        reject(@"ERROR", @"Expected hostname.", NULL);
    }
    else
    {
        if (username == nil && password == nil)
        {
            username = @"anonymous";
            password = @"anonymous@";
        }
        NSArray* address = [hostname componentsSeparatedByString:NSLocalizedString(@":", nil)];
        if ([address count] == 2) {
            NSString* host = [address objectAtIndex:0];
            NSNumber* port = [NSNumber numberWithInt:[[address objectAtIndex:1] intValue]];
            self.client = [FTPClient clientWithHost:host port:(int)port username:username password:password];
        } else {
            self.client = [FTPClient clientWithHost:hostname port:21 username:username password:password];
        }
        resolve(@(true));
    }
}

RCT_EXPORT_METHOD(list:(NSString *)path listWithResolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    if (path == nil)
    {
        reject(@"ERROR", @"Expected path.", NULL);
    }
    else
    {
        if ([path characterAtIndex:path.length - 1] != '/')
        {
            path = [path stringByAppendingString:@"/"];
        }
        [self.client listContentsAtPath:path showHiddenFiles:YES success:^(NSArray *contents) {
            NSMutableArray* files = [[NSMutableArray alloc] init];

            for (FTPHandle *handle in contents) {
                if (handle.type == FTPHandleTypeFile) {
                    NSMutableDictionary* newFile = [[NSMutableDictionary alloc] init];
                    // file name
                    NSString* name = handle.name;
                    [newFile setObject:name forKey:@"name"];

                    //NSNumber* type = handle.type;
                    NSNumber* size = [NSNumber numberWithUnsignedLongLong: handle.size];
                    [newFile setObject:size forKey:@"size"];

                    // modified date
                    NSDate* modifiedDate = handle.modified;
                    NSDateFormatter* dateFormatter = [[NSDateFormatter alloc] init];
                    dateFormatter.locale = [NSLocale localeWithLocaleIdentifier:@"en_US_POSIX"];
                    dateFormatter.dateFormat = @"yyyy-MM-dd'T'HH:mm:ss.SSSZ";
                    NSString* modifiedDateString = [dateFormatter stringFromDate:modifiedDate];
                    [newFile setObject:modifiedDateString forKey:@"modifiedDate"];

                    // Add this file to file list
                    [files addObject:newFile];
                } else if (handle.type == FTPHandleTypeDirectory) {
                    // Do something with directory.
                }
            }

            resolve(files);
        } failure:^(NSError *error) {
            reject(@"ERROR", @"List error", error);
        }];
    }
}

RCT_EXPORT_METHOD(downloadFile:(NSString *)remoteFile localFile:(NSString *)localFile downloadFileWithResolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    if ([localFile length] == 0 || [remoteFile length] == 0)
    {
        reject(@"ERROR", @"Expected localFile and remoteFile.", NULL);
    }
    else
    {
        [self.client downloadFile:remoteFile to:localFile progress:NULL success:^(void) {
            // Success!
            resolve(@(true));
        } failure:^(NSError *error) {
            reject(@"ERROR", @"Download file error", error);
        }];
    }
}

RCT_EXPORT_METHOD(disconnect:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    resolve(@(true));
}

@end

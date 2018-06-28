import boto3

s3 = boto3.resource('s3')

# Get list of objects for indexing
images = [('/Users/arunrajan/Pictures/1600mentzer.jpg', 'Mike Mentzer'),
          ('/Users/arunrajan/Pictures/Arun-maine.jpg', 'N590502'),
          ('/Users/arunrajan/Pictures/CROPPED-DSCF7069.jpg', 'N590502')]

# Iterate through list to upload objects to S3
for image in images:
  file = open(image[0], 'rb')
  object = s3.Object('eagle-eye-validator',
                     'index/' + image[0].lstrip('/Users/arunrajan/Pictures/'))
  ret = object.put(Body=file, Metadata={'FullName': image[1]})
  # print(s3.Bucket("eagle-eye-validator").download_file('index/1600mentzer.jpg',
  #                                                      '1600mentzer.jpg'))

  print ret

# radyfy-api-common-lib

Project for crm api commons

## Recent Fixes

### JavaScript Engine NullPointerException Fix

**Problem**: The application was throwing a `NullPointerException` in the `Utils.getShowConditionValue` method due to the JavaScript engine (Nashorn) being removed from Java 15+.

**Solution**: 
1. Added GraalJS dependencies to provide a modern JavaScript engine
2. Enhanced error handling with fallback behavior
3. Added thread-safe engine availability checking

**Files Modified**:
- `src/main/java/com/radyfy/common/utils/Utils.java` - Enhanced error handling
- `pom.xml` - Added GraalJS dependencies

The fix ensures graceful degradation when JavaScript evaluation is not available, preventing application crashes while maintaining functionality.

## Documentation

- [SendGrid Email Integration](SendGrid.md) - Complete guide for SendGrid email service integration and dynamic template usage

## Getting Started

First, clean the project:

```bash
mvn clean
```
chagne
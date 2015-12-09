declare module VsoTaskLib {
    interface VsoTaskCommon {
        
        // String convenience
        setStdStream(stdStream: NodeJS.WritableStream): void;
        setErrStream(errStream: NodeJS.WritableStream): void;
        exit(code: number): void;
        
        getVariable(name: string): string;
        getInput(name: string, required?: boolean): string;
        getDelimitedInput(name: string, delim: string, required?: boolean): string[];
        getPathInput(name: string, required?: boolean, check?: boolean): string;
        
        warning(message: string): void;
        error(message: string): void;
        debug(message: string): void;
        command(command: string, properties?: any, message?: any): void;
        
        cd(path: string): void;
        pushd(path: string): void;
        popd(): void;
        
        checkPath(path: string, name: string): void;
        mkdirP(path: string): void;
        which(tool: string, check?: boolean): string;
        cp(options: any, source: string, dest: string): void;
        
        match(list: string[], pattern: string, options?: any): string[];
        matchFile(file: string, pattern: string, options?: any): boolean;
        filter(pattern: string, options?: any): (file: string) => boolean;
        
        getEndpointUrl(id: string, optional: boolean): string;
        getEndpointAuthorization(id: string, optional: boolean): EndpointAuthorization;
    }
    
    export interface EndpointAuthorization {
        parameters: {
            [key: string]: string;
        };
        scheme: string;
    }
    
    // var TaskCommand: any;
    // var commandFromString: any;
    // var ToolRunner: any;
}

declare var vsotasklib: VsoTaskLib.VsoTaskCommon;

declare module "vso-task-lib" {
    export = vsotasklib;
}